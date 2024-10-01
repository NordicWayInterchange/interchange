package no.vegvesen.ixn.napcore.model;

public class AddCommentRequest {

    private String id;

    private String comment;

    public AddCommentRequest(){

    }

    public AddCommentRequest(String id, String comment) {
        this.id = id;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
     return "AddCommentRequest{" +
             "id=" + id +
             ", comment=" + comment +
             "}";
    }
}
