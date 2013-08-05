package play.mvc;

import play.libs.F;

/**
 * Created with IntelliJ IDEA.
 * User: alexandre
 * Date: 05/08/13
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
public abstract class ServerSentEventChunk extends Results.StringChunks {

    private Results.Chunks.Out<String> out;

    public void onReady(Results.Chunks.Out<String> out) {
        this.out = out;
        onReady(this);
    }

    public abstract void onReady(ServerSentEventChunk sse);

    public void sendMessage(String message) {
        out.write("data: "+message+"\n\n");
    }

    public void onDisconnected(F.Callback0 callback){
        out.onDisconnected(callback);
    }

}
