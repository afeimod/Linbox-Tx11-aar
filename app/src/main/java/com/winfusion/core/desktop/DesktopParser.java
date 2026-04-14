package com.winfusion.core.desktop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.desktop.exception.BadDesktopFormatException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

/**
 * Linux 的 .desktop 文件的解析类。
 */
public final class DesktopParser {

    private static final String SECTION_DESKTOP = "Desktop Entry";
    private static final String KEY_NAME = "Name";
    private static final String KEY_EXEC = "Exec";
    private static final String KEY_ICON = "Icon";

    private DesktopParser() {

    }

    /**
     * 解析 .desktop 文件并生成 {@link Desktop} 对象。
     *
     * @param filePath 要解析的文件的路径
     * @return Desktop 对象
     * @throws IOException               如果遇到读写错误
     * @throws BadDesktopFormatException 如果要解析的文件格式错误。
     */
    @NonNull
    public static Desktop parse(@NonNull Path filePath) throws IOException, BadDesktopFormatException {
        List<String> lines = Files.readAllLines(Paths.get(filePath.toAbsolutePath().toString()));

        String name = null;
        String exec = null;
        String iconName = null;
        String section = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                // skip empty lines and comments
                continue;
            } else if (line.startsWith("[") && line.endsWith("]")) {
                // read section
                section = line.substring(1, line.length() - 1);
            } else {
                if (section == null)
                    throw new BadDesktopFormatException("Get key without section: " + line);
                if (!Objects.equals(section, SECTION_DESKTOP))
                    continue;

                int index = line.indexOf("=");
                if (index == -1)
                    throw new BadDesktopFormatException("Bad format: " + line);
                else if (line.length() <= index + 1)
                    continue;

                String key = line.substring(0, index);
                String value = line.substring(index + 1);

                switch (key) {
                    case KEY_NAME -> {
                        if (name != null) {
                            throw new BadDesktopFormatException(
                                    "Get Name twice, last is:" + name + " , current is:" + value);
                        }
                        name = value;
                    }
                    case KEY_EXEC -> {
                        if (exec != null) {
                            throw new BadDesktopFormatException(
                                    "Get Exec twice, last is:" + exec + " , current is:" + value);
                        }
                        exec = value;
                    }
                    case KEY_ICON -> {
                        if (iconName != null) {
                            throw new BadDesktopFormatException(
                                    "Get Icon twice, last is:" + iconName + " , current is:" + value);
                        }
                        iconName = value;
                    }
                }
            }
        }

        if (name == null || exec == null) {
            throw new BadDesktopFormatException(".desktop file must contains Name and Exec sections: " +
                    filePath);
        }

        return new Desktop(name, exec, iconName);
    }

    /**
     * 从执行命令中获取 Wine 的执行目标。
     * 接受的执行命令需要是合法的 Wine 命令，例如对于文本（不是字符串） {@code "wine c:\\\\Program\\ Files\\ \\(x86\\)\\\\test.exe"}，
     * 会得到 {@code  "c:\Program Files (x86)\test.exe"}。
     *
     * @param exec 执行命令
     * @return 如果是合法的 Wine 命令，则返回执行目标，否则返回 null
     */
    @Nullable
    public static String getWineTargetFromExec(@NonNull String exec) {
        String match = " wine ";
        int index = exec.indexOf(match);

        if (index == -1 || exec.length() <= index + match.length())
            return null;

        return exec.substring(index + match.length())
                .replaceAll("\\\\([^\\\\]+)", "$1")
                .replaceAll("\\\\([^\\\\]+)", "$1")
                .replaceAll("\\\\\\\\", "\\\\")
                .trim();
    }
}
