class HttpRequest {
    private String header = "";
    private String body = "";
    private Session.httpMethods httpMethod = null;

    void setHeader(String header) {
        this.header = header;
    }

    void setBody(String body) {
        this.body += body;
    }

    Session.httpMethods getHttpMethod() {
        return httpMethod;
    }

    void setHttpMethod(Session.httpMethods httpMethod) {
        this.httpMethod = httpMethod;
    }

    String getHeader() {
        return header;
    }

    String getBody() {
        return body;
    }


}
