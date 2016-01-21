
package com.alvin.myhealth.Util;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by alvin on 2015/11/18.
 */
public class AppTool {
    /**
     * Get Now Time String as Format "yyyy/MM/dd HH:mm:ss"
     * @return time of String as Format "yyyy/MM/dd HH:mm:ss"
     */
    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Long current = System.currentTimeMillis();
        Date date = new Date(current);
        String nowTimeString = format.format(date);
        return nowTimeString;
    }

    /**
     * Get Nodes that connected with phone,such as your android watch
     * this method can't be used in UI thread
     * @return nodes
     */
    public static List<Node> getNodes(GoogleApiClient googleApiClient) {
        ArrayList<Node> results= new ArrayList<Node>();
        //get all connected nodes
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
        for (Node node : nodes.getNodes()) {
            results.add(node);
        }
        return results;
    }
}
