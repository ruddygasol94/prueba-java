public class Error {
    private String message;

    private int status;

    public Error(String p_message, int p_status) {
        this.message = p_message;
        this.status = p_status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int p_status) {
        this.status = p_status;
    }
}
