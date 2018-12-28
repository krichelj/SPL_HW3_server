package bgu.spl.net.api;

import java.io.Serializable;

public interface BGSCommand extends Serializable {

    Serializable execute();
}
