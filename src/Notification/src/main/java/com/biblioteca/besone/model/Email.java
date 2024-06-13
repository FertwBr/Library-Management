package com.biblioteca.besone.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    private String owner;
    private String emailFrom;
    private String emailTo;
    private String subject;
    private String text;

}
