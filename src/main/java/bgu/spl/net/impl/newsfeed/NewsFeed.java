package bgu.spl.net.impl.newsfeed;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NewsFeed {

    // a field for the channels and their queue of strings
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> channels = new ConcurrentHashMap<>();

    /** Fetches all the news that were published to a specific channel
     * @param channel the desired channel
     * @return a list of a arrays of strings
     */
    public ArrayList<String> fetch(String channel) {

        ConcurrentLinkedQueue<String> queue = channels.get(channel);

        if (queue == null) {
            return new ArrayList<>(0); //empty
        } else {
            return new ArrayList<>(queue); //copy of the queue, array list is serializable
        }
    }

    /** Publishes news to a channel by its name
     * @param channel the channel to publish to
     * @param news the news to publish
     */
    public void publish(String channel, String news) {

        ConcurrentLinkedQueue<String> queue = channels.computeIfAbsent(channel, k -> new ConcurrentLinkedQueue<>());
        queue.add(news);
    }

    /**
     * Clears the news feed
     */
    public void clear() {

        channels.clear();
    }
}
