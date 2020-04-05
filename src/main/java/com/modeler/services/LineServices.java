package com.modeler.services;

import org.springframework.beans.factory.annotation.Autowired;

import com.modeler.exceptions.ModelerException;
import com.modeler.model.Linea;
import com.modeler.repositories.LineRepository;

public class LineServices {
	@Autowired
	private LineRepository repo;
	
	public void save(Linea linea) throws ModelerException{
		if(linea.getModelo()==null || linea.getModelo().getLinea(linea)!=null) throw new ModelerException(ModelerException.existeYaEstaLinea);
		repo.save(linea);
	}
}
