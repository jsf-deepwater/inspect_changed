package x.datautil;

import java.util.List;

public interface XDbManager {
    void writeLog(String info);
    List<LogInfo> getLog(long from, long to);
    class LogInfo {
        public long time;
        public String info;

        public LogInfo(long time, String info) {
            this.time = time;
            this.info = info;
        }
    }
}
