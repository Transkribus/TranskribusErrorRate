/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.transkribus.errorrate.interfaces;

import eu.transkribus.errorrate.types.Count;
import eu.transkribus.errorrate.util.ObjectCounter;
import java.util.List;

/**
 *
 * @author gundram
 */
public interface IErrorModule {

    public void calculate(String reco, String ref);

    public List<String> getResults();

    public ObjectCounter<Count> getCounter();

    public void reset();
}
