package com.cocay.sicecd.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.cocay.sicecd.model.Grupo;
import com.cocay.sicecd.model.Inscripcion;
import com.cocay.sicecd.repo.InscripcionRep;


@Controller
public class ConsultaInscripcionController {
	@Autowired
	InscripcionRep ins;
	
	@RequestMapping(value = "/consultaInscripcion", method = RequestMethod.GET)
	public String consultaCurso(Model model) {
		return "ConsultarInscripcion/consultaInscripcion";
	}
	
	@RequestMapping(value = "/consultaInsGrupo", method = RequestMethod.POST)
	public ModelAndView consultarInsGrupo(ModelMap model,HttpServletRequest request) {
		String grupo = request.getParameter("ins_grupo");
		Inscripcion ins_1 = ins.findByClaveGrupo(Integer.parseInt(grupo)); 
		
		if(ins_1 != null) {
			model.put("ins", ins_1);
			return new ModelAndView("ConsultarInscripcion/muestraListaIns",model);
		}else {
			return new ModelAndView("/Avisos/ErrorBusqueda");
		}
	}
}
