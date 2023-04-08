package com.example.Tools.Filtration;

public class Word implements Comparable<Word>{
    public char c;
    public thewordList next = null;
    public Word(char c){
        this.c = c;
    }
    @Override
    public int compareTo(Word word) {
        return c - word.c;
    }
}