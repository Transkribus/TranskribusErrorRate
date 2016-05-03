package eu.transkribus.errorrate.interfaces;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.List;

/**
 * The tokenizer should split the string into its atomic tokens
 *
 * @author gundram
 */
public interface ITokenizer {

    /**
     * tokenize sequence into atomic tokens
     *
     * @param string  input string
     * @return list of tokens, without delimiters/separators
     */
    public List<String> tokenize(String string);

}
