package com.winfusion.feature.manager;

public interface Installable {

    void install() throws InstallFailedException;

    class InstallFailedException extends Exception {

        public InstallFailedException(Throwable cause) {
            super(cause);
        }
    }
}
