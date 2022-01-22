package com.wif.baseservice.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class CpuUsage {
    public static final String TAG = "CpuUsage";

    private static final String procPath = File.separator + "proc" + File.separator + "stat";

    public static String getTotalCpuPercent() {
        CPUTime startTime = new CPUTime();
        CPUTime endTime = new CPUTime();

        getCpuTime(startTime);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        getCpuTime(endTime);

        double cpuUsage;
        long totalTime = endTime.getTotalTime() - startTime.getTotalTime();
        if (totalTime == 0) {
            cpuUsage = 0;
        } else {
            cpuUsage = 1 - (((double) (endTime.getIdleTime() - startTime.getIdleTime())) / totalTime);
        }

        DecimalFormat df = new DecimalFormat("#.00");
        String cpuPercent = df.format(cpuUsage * 100) + "%";
        Log.i(TAG, "Total Cpu usage: " + cpuPercent);
        return cpuPercent;
    }

    /**
     * rk3288:/ $ cat proc/stat
     * cpu  user， nice, system, idle, iowait, irq, softirq
     * cpu  760312 5997 621318 21565554 1570 0 2141 0 0 0
     * cpu0 240025 2942 202601 4909313 388 0 2052 0 0 0
     * cpu1 141956 1807 116389 5591056 398 0 81 0 0 0
     * cpu2 234665 1105 186383 5447347 653 0 4 0 0 0
     * cpu3 143664 141 115944 5617837 129 0 1 0 0 0
     * intr 526443210 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 20095477 0 0 0 0 0 0 0 0 0 2244 0 12 1439508 205138 535896 13917405 0 0 1125 0 0 0 0 0 0 0 318862 480698272 10140 363 0 0 0 0 0 0 0 1491655 0 0 12 0 0 0 25741 1 111719 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 3 0 0 0 0
     * ctxt 36542644
     * btime 1642759234
     * processes 472124
     * procs_running 5
     * procs_blocked 0
     * softirq 41334096 657 18044997 11790 7107 0 0 1519109 6759219 0 14991217
     */
    private static void getCpuTime(CPUTime t) {
        BufferedReader fr = null;
        try {
            fr = new BufferedReader(new FileReader(procPath));

            String oneLine;
            while ((oneLine = fr.readLine()) != null) {
                if (oneLine.startsWith("cpu ")) {
                    String[] vals = oneLine.substring(4).trim().split(" ");
                    if (vals.length == 10) {
                        t.setTotalTime(Long.parseLong(vals[0])  // user
                                + Long.parseLong(vals[1])       // nice
                                + Long.parseLong(vals[2])       // system
                                + Long.parseLong(vals[3])       // idle
                                + Long.parseLong(vals[4])       // iowait
                                + Long.parseLong(vals[5])       // irq
                                + Long.parseLong(vals[6]));     // softirq
                        t.setIdleTime(Long.parseLong(vals[3]));
                        break;
                    } else {
                        Log.e(TAG, "parse error: " + oneLine);
                    }
                }
            }
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class CPUTime {
        private long totalTime;
        private long idleTime;

        public CPUTime() {
            totalTime = 0;
            idleTime = 0;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }

        public long getIdleTime() {
            return idleTime;
        }

        public void setIdleTime(long idleTime) {
            this.idleTime = idleTime;
        }
    }
}
