import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Filter {
    private ConcurrentHashMap<String, Integer> hashMap = new ConcurrentHashMap();
    private int period;
    private int limit;

    Filter(int limit, int period) {
        this.period = period;
        this.limit = limit;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (String s : hashMap.keySet()) {
                        hashMap.put(s, limit);
                    }
                    try {
                        Thread.sleep(period * 1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }

    public boolean filter(String ip) {
        if (hashMap.get(ip) != null) {
            Integer integer = hashMap.get(ip);
            if (integer == 0) {
                return false;
            }
            hashMap.put(ip, integer - 1);
        } else {
            hashMap.put(ip, limit - 1);
        }
        System.out.println("limit = " + hashMap.get(ip));
        return true;
    }
}
