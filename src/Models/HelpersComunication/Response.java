package Models.HelpersComunication;

public class Response<T> {

    public StatusResponse status;
    public RequestType type;
    public String message;
    public T data;

    public Response(StatusResponse status, RequestType type, String message, T data) {
        this.status = status;
        this.type = type;
        this.message = message;
        this.data = data;
    }

    public Response(StatusResponse status, RequestType type, T data) {
        this(status, type, null, data);
    }

    public Response(StatusResponse status, RequestType type, String message) {
        this(status, type, message, null);
    }

    public Response(StatusResponse status, RequestType type) {
        this(status, type, null, null);
    }

    public Response(StatusResponse status, String message) {
        this(status, null, message, null);
    }

    public Response(RequestType type) {
        this(null, type, null, null);

    }

    public Response() {

    }

}

