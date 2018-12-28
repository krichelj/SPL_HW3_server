package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.impl.rci.Command;
import java.io.Serializable;

/**
 * The FetchNewsCommand fetches all the news that were published to a specific channel
 */
public class FetchNewsCommand implements Command<NewsFeed> {

    private String channel;

    public FetchNewsCommand(String channel) {

        this.channel = channel;
    }

    @Override
    public Serializable execute(NewsFeed feed) {

        return feed.fetch(channel);
    }

}
