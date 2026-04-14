package com.winfusion.core.shell;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.shell.exception.ShellRuntimeException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 终端执行器类，用于执行终端命令。
 */
public class ShellExecutor {

    private static final String TAG = "Shell";

    private final HashMap<String, String> environment = new HashMap<>();
    private final ArrayList<String> command = new ArrayList<>();
    private Path workingDir;
    private Process process;
    private CompletableFuture<Void> stdOutFuture;
    private CompletableFuture<Void> stdErrFuture;
    private CompletableFuture<Integer> execFuture;
    private Consumer<String> stdOutConsumer;
    private Consumer<String> stdErrConsumer;
    private Consumer<Integer> exitConsumer;

    /**
     * 设定命令。
     *
     * @param command 命令数组
     * @return 自身
     */
    @NonNull
    public ShellExecutor setCommand(@NonNull String... command) {
        this.command.clear();
        for (String c : command) {
            if (c != null && !c.isBlank())
                this.command.add(c);
        }
        return this;
    }

    /**
     * 获取命令。
     *
     * @return 命令数组
     */
    @NonNull
    public String[] getCommand() {
        return command.toArray(new String[0]);
    }

    /**
     * 添加环境变量。
     *
     * @param varName  变量名
     * @param varValue 变量值
     * @return 自身
     */
    @NonNull
    public ShellExecutor putEnv(@NonNull String varName, @NonNull String varValue) {
        environment.put(varName, varValue);
        return this;
    }

    /**
     * 添加环境变量。
     *
     * @param envMap 变量映射表
     * @return 自身
     */
    @NonNull
    public ShellExecutor putEnv(@NonNull Map<String, String> envMap) {
        environment.putAll(envMap);
        return this;
    }

    /**
     * 删除一个环境变量。
     *
     * @param envName 变量名
     * @return 自身
     */
    @NonNull
    public ShellExecutor removeEnv(@NonNull String envName) {
        environment.remove(envName);
        return this;
    }

    /**
     * 获取环境变量值。
     *
     * @param varName 变量名
     * @return 如果变量存在则返回变量值，否则返回 null
     */
    @Nullable
    public String getEnv(@NonNull String varName) {
        return environment.get(varName);
    }

    /**
     * 设置工作目录。
     *
     * @param workingDir 工作目录路径
     * @return 自身
     */
    @NonNull
    public ShellExecutor setWorkingDir(@NonNull Path workingDir) {
        this.workingDir = workingDir;
        return this;
    }

    /**
     * 获取当前工作目录。
     *
     * @return 如果已设定工作目录则返回路径，否则返回 null
     */
    @Nullable
    public Path getWorkingDir() {
        return workingDir;
    }

    /**
     * 设置标准输出回调。
     *
     * @param stdOutConsumer 标准输出回调
     * @return 自身
     */
    @NonNull
    public ShellExecutor onStdOut(@Nullable Consumer<String> stdOutConsumer) {
        this.stdOutConsumer = stdOutConsumer;
        return this;
    }

    /**
     * 设置错误输出回调。
     *
     * @param stdErrConsumer 错误输出回调
     * @return 自身
     */
    @NonNull
    public ShellExecutor onStdErr(@Nullable Consumer<String> stdErrConsumer) {
        this.stdErrConsumer = stdErrConsumer;
        return this;
    }

    /**
     * 设置返回值回调。
     *
     * @param exitConsumer 返回值回调
     * @return 自身
     */
    @NonNull
    public ShellExecutor onExit(@Nullable Consumer<Integer> exitConsumer) {
        this.exitConsumer = exitConsumer;
        return this;
    }

    /**
     * 启动执行线程。
     *
     * @return 自身
     * @throws IOException           如果遇到读写错误
     * @throws ShellRuntimeException 如果执行线程正在运行
     */
    @NonNull
    public ShellExecutor exec() throws IOException {
        if (process == null) {
            ProcessBuilder builder = new ProcessBuilder();
            builder.environment().putAll(environment);
            builder.command(command);
            builder.directory(workingDir == null ? null : workingDir.toFile());
            process = builder.start();

            stdOutFuture = startStdOutFuture();
            stdErrFuture = startStdErrFuture();
            execFuture = startExecFuture();
        } else {
            throw new ShellRuntimeException("Process is already running.");
        }
        return this;
    }

    /**
     * 判断执行线程是否正在运行。
     *
     * @return 如果执行线程正在运行则返回 true，如果未创建或者已经结束则返回 false
     */
    public boolean isRunning() {
        return process != null && process.isAlive();
    }

    /**
     * 停止执行线程。
     *
     * @param force 是否强制停止
     */
    public void stop(boolean force) {
        if (process != null) {
            if (force)
                process.destroyForcibly();
            else
                process.destroy();
        }
    }

    /**
     * 等待执行线程完成，并返回结果。
     *
     * @return 结果
     * @throws InterruptedException  如果遇到中断错误
     * @throws ShellRuntimeException 如果执行线程未创建
     */
    public int waitFor() throws InterruptedException {
        if (execFuture == null)
            throw new ShellRuntimeException("Process is not running.");
        return execFuture.join();
    }

    @NonNull
    private CompletableFuture<Void> startStdOutFuture() {
        return CompletableFuture.runAsync(
                () -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (stdOutConsumer != null)
                                stdOutConsumer.accept(line);
                        }
                    } catch (InterruptedIOException e) {
                        Log.d(TAG, "stdout read interrupted due to process destroy.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).exceptionally(
                e -> {
                    Log.d(TAG, "std_out exception.", e);
                    return null;
                }
        );
    }

    @NonNull
    private CompletableFuture<Void> startStdErrFuture() {
        return CompletableFuture.runAsync(
                () -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (stdErrConsumer != null)
                                stdErrConsumer.accept(line);
                        }
                    } catch (InterruptedIOException e) {
                        Log.d(TAG, "stderr read interrupted due to process destroy.");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ).exceptionally(e -> {
            Log.d(TAG, "std_err exception.", e);
            return null;
        });
    }

    @NonNull
    private CompletableFuture<Integer> startExecFuture() {
        return CompletableFuture.supplyAsync(() -> {
            int exitCode;
            try {
                exitCode = process.waitFor();
                stdOutFuture = null;
                stdErrFuture = null;
                process = null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return exitCode;
        }).handle((i, e) -> {
            if (e != null) {
                Log.d(TAG, "exec exception.", e);
                i = 0;
            }
            if (exitConsumer != null)
                exitConsumer.accept(i);
            return i;
        });
    }
}
