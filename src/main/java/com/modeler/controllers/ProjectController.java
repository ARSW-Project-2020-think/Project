package com.modeler.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.modeler.exceptions.ModelerException;
import com.modeler.model.Modelo;
import com.modeler.model.Proyecto;
import com.modeler.model.Rectangulo;
import com.modeler.model.Usuario;
import com.modeler.model.Version;
import com.modeler.services.ModelServices;
import com.modeler.services.ProjectServices;
import com.modeler.services.RectangleServices;
import com.modeler.services.UserServices;
import com.modeler.services.VersionServices;

import java.util.List;

@RestController
@CrossOrigin(origins="*")
@RequestMapping(value="/projectapi")
public class ProjectController {
	
	@Autowired
	private ProjectServices projectServices;
	@Autowired
	private UserServices userServices;
	@Autowired
	private VersionServices versionServices;
	@Autowired
	private ModelServices modelServices;
	@Autowired
	private RectangleServices rectangleServices;
	
	@RequestMapping(value="/{username}/project",method=RequestMethod.POST)
	public ResponseEntity<?> addProject(@PathVariable String username,@RequestBody Proyecto proyecto, Authentication auth){
		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}
		Usuario u=null;
		try {
			u = userServices.getUsuarioByUsername(username);
			Proyecto p = new Proyecto();
			p.setNombre(proyecto.getNombre());
			p.setPublico(proyecto.getPublico());
			p.setAutor(u);
			projectServices.add(p);
			Version v = new Version(1);
			u = userServices.getUsuarioByUsername(username);
			v.setProyecto(u.getProyectoByName(proyecto.getNombre()));
			versionServices.save(v);
			
		} catch (ModelerException e) {
			//System.out.println(">>>>>>>>>> error"+e.getMessage());
			return new ResponseEntity<>("Error, No project add",HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
	
	
	
	@RequestMapping(method=RequestMethod.GET)
	public ResponseEntity<?> getAll(){		
		return new ResponseEntity<>(projectServices.getAll(),HttpStatus.OK);
	}
	
	
	
	
	@RequestMapping(value="/{username}/project",method=RequestMethod.GET)
	public ResponseEntity<?> getAllProjectsByUser(@PathVariable String username, Authentication auth){
		Usuario u = userServices.getUsuarioByUsername(username);
		if (!u.getCorreo().equals(auth.getName())) {
			//System.out.println("entro 1");
			//System.out.println(projectServices.getPublicProjectsByusuario(username));
			return new ResponseEntity<>(projectServices.getPublicProjectsByusuario(username),HttpStatus.OK);
		}
		//System.out.println("entro 2");
		//System.out.println(u.getProyectos());
		return new ResponseEntity<>(u.getProyectos(),HttpStatus.OK);
	}
	
	
	@RequestMapping(value="/{username}/project/{project}/version/{version}/modelo", method = RequestMethod.POST)
	public ResponseEntity<?> saveModel(@PathVariable String username,@PathVariable String project,@PathVariable int version,@RequestBody Modelo modelo,Authentication auth){
		List<Proyecto> proyectosCompartidos= userServices.getUsuario(auth.getName()).getProyectosCompartidos();

		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())&&!userServices.esColaborador(username,project,proyectosCompartidos)) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}

		try {
			Usuario u = userServices.getUsuarioByUsername(username);
			Version v = u.getVersion(project,version);
			Modelo m = new Modelo(modelo.getNombre(),v,modelo.getTipo());
			modelServices.save(m);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (ModelerException e) {
			//System.out.println(e.getMessage());
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	
	@RequestMapping(value="/{username}/project/{project}/version/{version}/modelo", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteModelo(@PathVariable String username,@PathVariable String project,@PathVariable int version,@RequestBody Modelo modelo,Authentication auth){
		Modelo m = modelServices.getModelById(modelo.getId());
		
		/*if(m==null || modelo.getVersion().getNumero()!= version || !modelo.getVersion().getProyecto().getAutor().getUsername().equals(username)
				|| !modelo.getVersion().getProyecto().getAutor().getCorreo().equals(auth.getName())) {
			System.out.println("NETRO 2");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}*/
		try {
			modelServices.delete(m);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (ModelerException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
	@RequestMapping(value="/{username}/project/{project}/version/",method=RequestMethod.POST)
	public ResponseEntity<?> addVersion(@PathVariable String username,@PathVariable String project,@RequestBody Version version){
		Proyecto p = userServices.getUsuarioByUsername(username).getProyectoByName(project);
		Version v = p.findLastVersion();
		Version v2 = v.clone();
		v2.setNumero(version.getNumero());
		versionServices.save(v2);
		v2.setProyecto(p);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}
	
	@RequestMapping(value="/{username}/project/{project}/version/{version}/modelo",method=RequestMethod.GET)
	public ResponseEntity<?> getModelos(@PathVariable String username,@PathVariable String project,@PathVariable int version,Authentication auth){
		List<Proyecto> proyectosCompartidos= userServices.getUsuario(auth.getName()).getProyectosCompartidos();

		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())&&!userServices.esColaborador(username,project,proyectosCompartidos)) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(userServices.getUsuarioByUsername(username).getModelos(project,version),HttpStatus.ACCEPTED);
	}
	
	@RequestMapping(value="/{username}/project/{project}/version/{version}/modelo/{modelname}",method=RequestMethod.GET)
	public ResponseEntity<?> getModelo(@PathVariable String username,@PathVariable String project,@PathVariable int version,@PathVariable String modelname,Authentication auth){
		List<Proyecto> proyectosCompartidos= userServices.getUsuario(auth.getName()).getProyectosCompartidos();

		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())&&!userServices.esColaborador(username,project,proyectosCompartidos)) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}

		return new ResponseEntity<>(userServices.getUsuarioByUsername(username).getModelo(project,version,modelname),
				HttpStatus.ACCEPTED);
	}
	
	
	@RequestMapping(value="/{username}/project/{project}/version/{version}/modelo/{modelname}/rectangle",method=RequestMethod.POST)
	public ResponseEntity<?> saveRectangle(@PathVariable String username,@PathVariable String project,@PathVariable int version,@PathVariable String modelname,@RequestBody Rectangulo rectangulo,Authentication auth){
		List<Proyecto> proyectosCompartidos= userServices.getUsuario(auth.getName()).getProyectosCompartidos();

		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())&&!userServices.esColaborador(username,project,proyectosCompartidos)) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}

		try {
			Modelo m = userServices.getUsuarioByUsername(username).getModelo(project,version,modelname);
			
			Rectangulo r = new Rectangulo(rectangulo.getNombre(),rectangulo.getX(),rectangulo.getY(),
					rectangulo.getAncho(),rectangulo.getAlto(),m);
			rectangleServices.save(r);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (ModelerException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	
	@RequestMapping(value="/{username}/project/{project}/version/{version}/modelo/{modelname}/rectangle",method=RequestMethod.PUT)
	public ResponseEntity<?> updateRectangle(@PathVariable String username,@PathVariable String project,@PathVariable int version,@PathVariable String modelname,@RequestBody Rectangulo rectangulo,Authentication auth){
		List<Proyecto> proyectosCompartidos= userServices.getUsuario(auth.getName()).getProyectosCompartidos();

		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())&&userServices.esColaborador(username,project,proyectosCompartidos)) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}

		try {
			Rectangulo m = userServices.getUsuarioByUsername(username).getRectangulo(project,version,modelname,rectangulo.getNombre());
			m.setX(rectangulo.getX());
			m.setY(rectangulo.getY());
			m.setAncho(rectangulo.getAncho());
			m.setAlto(rectangulo.getAlto());
			rectangleServices.update(m);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (ModelerException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	
	@RequestMapping(value="/{username}/projectshared",method=RequestMethod.GET)
	public ResponseEntity<?> getShareProjects(@PathVariable String username,Authentication auth) {
		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}
		return new ResponseEntity<>(userServices.getUsuarioByUsername(username).getProyectosCompartidos(),HttpStatus.ACCEPTED);
	}
	
	
	@RequestMapping(value="/{username}/share/{to}/project/{projectname}",method = RequestMethod.PUT)
	public ResponseEntity<?> addShareProject(@PathVariable String username,@PathVariable String to,@PathVariable String projectname ,Authentication auth){
		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}
		try {
			Proyecto p = userServices.getUsuarioByUsername(username).getProyectoByName(projectname);
			Usuario u = userServices.getUsuarioByUsername(to);
			u.addProyectoCompartido(p);
			userServices.update(u);
			u = userServices.getUsuarioByUsername(to);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (ModelerException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
	
	
	@RequestMapping(value="/{username}/colaborators/project/{projectname}",method = RequestMethod.GET)
	public ResponseEntity<?> getCollaborators(@PathVariable String username,@PathVariable String projectname,Authentication auth){
		if (!auth.getName().equals(userServices.getUsuarioByUsername(username).getCorreo())) {
			return new ResponseEntity<>("Error, FORBIDDEN add project",HttpStatus.FORBIDDEN);
		}
		Proyecto p = userServices.getUsuarioByUsername(username).getProyectoByName(projectname);
		p.getColaboradores();
		return new ResponseEntity<>(p.getColaboradores(),HttpStatus.ACCEPTED);
	}
	
	
	
	@RequestMapping(value="/{username}/colaborator/project/{projectname}",method=RequestMethod.DELETE)
	public ResponseEntity<?> deleteColaborator(@PathVariable String username,@PathVariable String projectname,@RequestBody Usuario usuario) {
		try {
			Usuario u = userServices.getUsuarioByUsername(username);
			Proyecto p = u.getProyectoByName(projectname);
			u = userServices.getUsuarioByUsername(usuario.getUsername());
			u.removeProyectoColaboracion(p);
			userServices.update(u);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (ModelerException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	@RequestMapping(value="/{username}/project",method=RequestMethod.DELETE)
	public ResponseEntity<?> deleteProject(@PathVariable String username,@RequestBody Proyecto proyecto,Authentication auth){
		try {
			Proyecto p = projectServices.getProjectById(proyecto.getId());
			if(p==null || !p.getAutor().getUsername().equals(username))
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			projectServices.delete(p);
			return new ResponseEntity<>(HttpStatus.ACCEPTED);
		} catch (ModelerException e) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}
	
}
