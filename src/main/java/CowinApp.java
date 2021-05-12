/**
 * @Author vvk30
 */

import model.Pojo;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import utils.SoundUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.stream.Collectors;

public class CowinApp {
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.out.println("USAGE: jar <pin-code> <interval-in-seconds>");
            System.out.println("Example: jar 590001 30");
            System.exit(1);
        }
        String pinCode = args[0];
        int timeLag = Integer.parseInt(args[1]);
        while (true) {
            Date date = new Date(System.currentTimeMillis());
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            URL url = new URL("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode="
                    + pinCode + "&date=" + formatter.format(date));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.8 (Windows NT 6.1; WOW64) AppleWebkit/537.11 (KHTML, like Gecko) Chrome/23.9.1271.95 Safari/537.11");
            connection.setRequestMethod("GET");
            InputStream stream = connection.getInputStream();
            String res = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            Pojo pojo = mapper.readValue(res, Pojo.class);
            pojo.getCenters().forEach(center -> {

                center.getSessions().forEach(session -> {
                    if (session.getMin_age_limit() == 18) {

                        System.out.println(center.getName());
                        System.out.println(session.getVaccine() + "-" + session.getAvailable_capacity());
                        System.out.println(session.getDate());
                        System.out.println(session.getSlots());
                        System.out.println("********************************");
                        if (session.getAvailable_capacity() > 0) {
                            try {
                                SoundUtils.main(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            });
            System.out.println("---------------- Retrying in " + timeLag + " seconds ----------------");
            Thread.sleep(timeLag * 1000L);
        }
    }
}