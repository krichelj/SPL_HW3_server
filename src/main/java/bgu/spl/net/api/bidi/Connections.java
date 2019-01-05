package bgu.spl.net.api.bidi;

/** This interface should map a unique ID for each active client
 *  connected to the server.
 * @param <T>
 */
public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
