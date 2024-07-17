package com.switix.roomservice.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Video {
    private String thumbnail;
    private String title;
    private String author;
    private String url;
}
