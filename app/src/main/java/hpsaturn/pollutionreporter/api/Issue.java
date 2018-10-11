package hpsaturn.pollutionreporter.api;

import android.support.annotation.NonNull;

public final class Issue {

    public Issue(String title, String body){
        this.title = title;
        this.body = body;
    }

    @NonNull
    private String title;

    @NonNull
    private String body;

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getBody() {
        return body;
    }
}
