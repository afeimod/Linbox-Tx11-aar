package com.winfusion.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.winfusion.R;
import com.winfusion.adapter.HomeSettingAdapter;
import com.winfusion.application.WinfusionApplication;
import com.winfusion.core.compression.ArchiveType;
import com.winfusion.core.compression.TarCompressor;
import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.core.elf.ElfFile;
import com.winfusion.core.elf.exception.ELFException;
import com.winfusion.core.mslink.ShellLink;
import com.winfusion.core.mslink.ShellLinkParser;
import com.winfusion.core.mslink.exception.BadLnkFormatException;
import com.winfusion.core.image.ico.exception.BadIconFormatException;
import com.winfusion.core.image.ico.IconDecoder;
import com.winfusion.core.pe.IconParser;
import com.winfusion.core.pe.exception.PEException;
import com.winfusion.core.shm.SHMServer;
import com.winfusion.core.shm.SHMServerCallback;
import com.winfusion.core.shm.driver.BaseSHMDriver;
import com.winfusion.core.shm.exception.SHMServerException;
import com.winfusion.core.soundfont.SoundFontInfo;
import com.winfusion.core.soundfont.SoundFontParser;
import com.winfusion.core.wayland.WestonActivity;
import com.winfusion.databinding.FragmentHomeSettingsBinding;
import com.winfusion.model.HomeSettingModel;
import com.winfusion.model.MainActivityViewModel;
import com.winfusion.utils.BitmapUtils;
import com.winfusion.utils.LaunchMode;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HomeSettingFragment extends Fragment {

    private FragmentHomeSettingsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeSettingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        MainActivityViewModel.updateRootViewToFitNavigationBar(root);
        MainActivityViewModel.setShowNavigation(requireActivity(), true);
        setupSettingsList();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    SHMServer shmServer;

    private void setupSettingsList() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),
                getResources().getInteger(R.integer.setting_list_grid_columns));
        binding.listSettings.setLayoutManager(gridLayoutManager);

        HomeSettingAdapter homeSettingAdapter = new HomeSettingAdapter(createSettingOptionModels());
        binding.listSettings.setAdapter(homeSettingAdapter);
    }

    @NonNull
    private ArrayList<HomeSettingModel> createSettingOptionModels() {
        ArrayList<HomeSettingModel> models = new ArrayList<>();

//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            ShellLink shellLink = null;
//                            try {
//                                shellLink = ShellLinkParser.parse("/sdcard/test.lnk", Charset.forName("GBK"));
//                            } catch (IOException | BadLnkFormatException e) {
//                                e.printStackTrace();
//                            }
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            ByteBuffer buffer;
//                            List<ByteBuffer> buffers;
//                            Bitmap bitmap;
//                            List<Bitmap> bitmaps = new ArrayList<>();
//                            try {
//                                buffer = IconParser.parseMainIconFromPE(Paths.get("/sdcard/test.exe"));
//                                bitmap = BitmapUtils.getBestBitmap(IconDecoder.parseBitmapsFromIcon(buffer));
//
//                                buffers = IconParser.parseAllIconFromPE(Paths.get("/sdcard/test.exe"));
//                                for (ByteBuffer b : buffers) {
//                                    bitmaps.addAll(IconDecoder.parseBitmapsFromIcon(b));
//                                }
//                            } catch (BadIconFormatException | PEException e) {
//                                e.printStackTrace();
//                                return;
//                            }
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            ElfFile elfFile = null;
//                            String interpreter = null;
//                            String soname = null;
//                            String[] rpath = null;
//                            String[] needed = null;
//
//                            try {
//                                elfFile = new ElfFile(Paths.get("/sdcard/test.so"));
//                            } catch (ELFException e) {
//                                e.printStackTrace();
//                            }
//
//                            try {
//                                interpreter = elfFile.getInterpreter();
//                            } catch (ELFException e) {
//                                e.printStackTrace();
//                            }
//
//                            try {
//                                soname = elfFile.getSoName();
//                            } catch (ELFException e) {
//                                e.printStackTrace();
//                            }
//
//                            try {
//                                rpath = elfFile.getRPath();
//                            } catch (ELFException e) {
//                                e.printStackTrace();
//                            }
//
//                            try {
//                                needed = elfFile.getNeeded();
//                            } catch (ELFException e) {
//                                e.printStackTrace();
//                            }
//
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.home)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            shmServer = new SHMServer("/data/data/com.winfusion/files/socket",
//                                    new SHMServerCallback() {
//                                        @Override
//                                        public void onRuntimeFatal(SHMServerException e) {
//                                            e.printStackTrace();
//                                        }
//
//                                        @Override
//                                        public BaseSHMDriver onRequireDriver(int clientType) {
//                                            return new BaseSHMDriver() {
//                                                @NonNull
//                                                @Override
//                                                public String name() {
//                                                    return "WinfusionTestDriver";
//                                                }
//
//                                                @Override
//                                                public boolean attach() {
//                                                    return true;
//                                                }
//
//                                                @Override
//                                                public void detach() {
//                                                    return;
//                                                }
//
//                                                @Override
//                                                public int getSharedMemoryFd() {
//                                                    return -1;
//                                                }
//                                            };
//                                        }
//
//                                        @Override
//                                        public void onDriverAttached(@NonNull BaseSHMDriver driver) {
//                                            Log.e("test", "SHM Driver attached!");
//                                        }
//
//                                        @Override
//                                        public void onDriverDetached(@NonNull BaseSHMDriver driver) {
//                                            Log.e("test", "SHM Driver detached!");
//                                        }
//                                    });
//                            try {
//                                shmServer.start(WinfusionApplication.getInstance().getExecutor());
//                            } catch (SHMServerException e) {
//                                e.printStackTrace();
//                            }
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            if (shmServer != null)
//                                shmServer.stop();
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            SoundFontParser parser = new SoundFontParser();
//                            try (FileInputStream stream = new FileInputStream("/sdcard/test.sf2")) {
//                                parser.load(stream);
//                            } catch (IOException | SoundFontParser.SoundFont2ParserException e) {
//                                Log.e("test", "failed", e);
//                            }
//                            SoundFontInfo info = parser.getInfo();
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            try {
//                                TarCompressor.compress(
//                                        ArchiveType.TAR_XZ,
//                                        1,
//                                        (source, dest, totalSize, currentRead) -> Log.e("test", source + " " + dest + " " + totalSize + " " + currentRead),
//                                        "test",
//                                        Paths.get("/sdcard/test1.tar.xz"),
//                                        Paths.get("/sdcard/test.so"),
//                                        Paths.get("/sdcard/test.sf2")
//                                );
//                            } catch (IOException | CompressorException e) {
//                                e.printStackTrace();
//                            }
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            try {
//                                TarCompressor.extract(
//                                        ArchiveType.TAR_XZ,
//                                        Paths.get("/sdcard/test.tar.xz"),
//                                        Paths.get("/sdcard/test"),
//                                        (source, dest, totalSize, currentRead) -> Log.e("test", source + " " + dest + " " + totalSize + " " + currentRead)
//                                );
//                            } catch (IOException | CompressorException e) {
//                                e.printStackTrace();
//                            }
//                            return;
//                        })
//                        .build()
//        );
//
//        models.add(
//                new HomeSettingModel.Builder()
//                        .setTitleId(R.string.test)
//                        .setDescriptionId(R.string.test)
//                        .setIconId(R.drawable.ic_dashboard)
//                        .setClickTask(() -> {
//                            Path source = Paths.get("/sdcard/test.tar.xz");
//                            Path target = Paths.get("test/test.so");
//
//                            try {
//                                try (InputStream in = TarCompressor.extractFile(ArchiveType.TAR_XZ, source, target)) {
//                                    Log.e("test", in.toString());
//                                }
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                            return;
//                        })
//                        .build()
//        );

        models.add(
                new HomeSettingModel.Builder()
                        .setTitleId(R.string.test)
                        .setDescriptionId(R.string.test)
                        .setIconId(R.drawable.ic_dashboard)
                        .setClickTask(() -> {
                            HomeSettingFragmentDirections.ActionToContents action =
                                    HomeSettingFragmentDirections.actionToContents(
                                            LaunchMode.Standalone.name(),
                                            null,
                                            null
                                    );
                            NavHostFragment.findNavController(this).navigate(action);
                        })
                        .build()
        );

        models.add(
                new HomeSettingModel.Builder()
                        .setTitleId(R.string.test)
                        .setDescriptionId(R.string.test)
                        .setIconId(R.drawable.ic_dashboard)
                        .setClickTask(() -> {
                            Intent intent = new Intent(requireContext(), WestonActivity.class);
                            startActivity(intent);
                        })
                        .build()
        );

        return models;
    }
}
