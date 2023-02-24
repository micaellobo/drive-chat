package Models.HelpersComunication;

public class Request<T> {

    public RequestType type;

//    public LocalDateTime datetime;

    public T data;

    public Request(RequestType type) {
        this.type = type;
    }

    public Request(RequestType requestType, T data) {
//        this.datetime = LocalDateTime.now();
        this.type = requestType;
        this.data = data;
    }
}
