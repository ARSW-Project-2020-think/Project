package com.modeler.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.modeler.model.Proyecto;

public interface ProjectRepository extends JpaRepository<Proyecto, Integer>{
	@Query(value = "SELECT * FROM proyecto p WHERE p.usuario=?1", nativeQuery = true)
	public List<Proyecto> getProyectoByusuario(String usuario);

}
