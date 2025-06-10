package com.example.datadownloadtool.util;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

public class HardwareCheckerUtil {
    public static String getSystemInfoText() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();

        String cpu = hal.getProcessor().getProcessorIdentifier().getName();
        long ramTotal = hal.getMemory().getTotal() / (1024 * 1024);
        long ramAvailable = hal.getMemory().getAvailable() / (1024 * 1024);

        StringBuilder diskInfo = new StringBuilder();
        for (HWDiskStore disk : hal.getDiskStores()) {
            String model = disk.getModel();
            long sizeGB = disk.getSize() / (1024 * 1024 * 1024);
            diskInfo.append("- ").append(model).append(" (").append(sizeGB).append(" GB)\n");
        }

        return String.format("""
                🖥️ CPU: %s
                💾 RAM: %d MB (Available: %d MB)
                🗃️ Disks:
                %s
                """, cpu, ramTotal, ramAvailable, diskInfo.toString().trim());
    }
}
