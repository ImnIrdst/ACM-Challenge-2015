package common.net;

public interface ReceiveListener {

    public void notify(final Command cmd, final NetworkManager netManager);

}
