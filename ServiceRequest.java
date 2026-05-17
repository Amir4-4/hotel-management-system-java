public class ServiceRequest {
    private int requestID;
    private int guestID;
    private String serviceType;
    private String requestDate;
    private String status;

    public ServiceRequest(int requestID, int guestID, String serviceType, String requestDate) {
        this.requestID = requestID;
        this.guestID = guestID;
        this.serviceType = serviceType;
        this.requestDate = requestDate;
        this.status = "Pending";
    }

    public void addRequest() { /* add */ }
    public void completeRequest() { status = "Completed"; }
    public void updateRequestStatus(String s) { status = s; }

    // getters
    public int getRequestID() { return requestID; }
    public int getGuestID() { return guestID; }
    public String getServiceType() { return serviceType; }
    public String getRequestDate() { return requestDate; }
    public String getStatus() { return status; }
}