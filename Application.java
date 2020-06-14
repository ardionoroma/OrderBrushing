import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Application {
    public static void main(String[] args) throws Exception {
        List<RecordDTO> records = new ArrayList<>();
        Map<Long, String> results = new HashMap<>();

        //Input the CSV file
        try (BufferedReader br = new BufferedReader(new FileReader("order_brush_order.csv"))) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String line;
            line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                //Convert from String to Object
                RecordDTO record = new RecordDTO();
                record.setOrderId(Long.parseLong(values[0]));
                record.setShopId(Long.parseLong(values[1]));
                record.setUserId(Long.parseLong(values[2]));
                record.setEventTime(df.parse(values[3]));
                records.add(record);
            }

            //Sort by event_time ASC
            Collections.sort(records);

            //Grouped by shopid
            Map<Long, List<RecordDTO>> recordsGrouped = records.stream().collect(Collectors.groupingBy(RecordDTO::getShopId));

            //For each shop...
            for (Map.Entry<Long, List<RecordDTO>> perShop : recordsGrouped.entrySet()) {

                //Grouped by hour
                Map<Integer, List<RecordDTO>> objectsPerHour = perShop.getValue().stream().collect(Collectors.groupingBy(RecordDTO::getHour));
                List<Long> suspectedUsers = new ArrayList<>();

                //For each hour...
                for (Map.Entry<Integer, List<RecordDTO>> perHour : objectsPerHour.entrySet()) {

                    //Distinct the buyer
                    List<Long> userids = perHour.getValue().stream().map(RecordDTO::getUserId).distinct().collect(Collectors.toList());
                    Map<Long, Integer> concurrences = new LinkedHashMap<>();

                    //If concurrence rate < 3 then skip this loop
                    if (perHour.getValue().size() / userids.size() < 3) {
                        continue;
                    }

                    //If concurrence rate >= 3, get the frequency of that userid in that hour
                    for (Long userid : userids) {
                        concurrences.put(userid, Collections.frequency(perHour.getValue(), userid));
                    }

                    //Sort the concurrences in descending order and get the suspected userid
                    concurrences.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
                    Integer max = concurrences.entrySet().iterator().next().getValue();
                    for (Map.Entry<Long, Integer> entry : concurrences.entrySet()) {
                        if (entry.getValue().equals(max)) {
                            suspectedUsers.add(entry.getKey());
                        }
                    }
                }

                //Convert the suspected buyers to String separated by '&'
                StringBuffer userIdsBuffer = new StringBuffer();
                if (suspectedUsers.size() > 0) {
                    for (Long user : suspectedUsers) {
                        userIdsBuffer.append(user);
                        userIdsBuffer.append("&");
                    }
                } else {
                    userIdsBuffer.append(0);
                }
                String userIdResults = userIdsBuffer.toString();
                if (userIdResults.substring(userIdResults.length() - 1).equals("&")){
                    userIdResults = userIdResults.substring(0, userIdResults.length() - 1);
                }

                //Put the shopid along with the userid into the final Map
                results.put(perShop.getKey(), userIdResults);
            }

            //Write to the output CSV
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("submissions.csv"), "UTF-8"));
            bw.write("shopid,userid");
            bw.newLine();
            for (Map.Entry<Long, String> entry : results.entrySet()) {
                StringBuffer oneLine = new StringBuffer();
                oneLine.append(entry.getKey());
                oneLine.append(",");
                oneLine.append(entry.getValue());
                bw.write(oneLine.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        }
    }
}