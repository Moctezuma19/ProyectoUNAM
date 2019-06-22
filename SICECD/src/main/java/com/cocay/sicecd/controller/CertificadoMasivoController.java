package com.cocay.sicecd.controller;

import com.cocay.sicecd.model.Curso;
import com.cocay.sicecd.model.Inscripcion;
import com.cocay.sicecd.LogTypes;
import com.cocay.sicecd.model.Certificado;
import com.cocay.sicecd.model.Profesor;
import com.cocay.sicecd.model.Url_ws;
import com.cocay.sicecd.repo.CertificadoRep;
import com.cocay.sicecd.repo.CursoRep;
import com.cocay.sicecd.repo.ProfesorRep;
import com.cocay.sicecd.repo.Url_wsRep;
import com.cocay.sicecd.security.pdf.SeguridadPDF;
import com.cocay.sicecd.service.Logging;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.json.*;

@Component
@PropertySource(ignoreResourceNotFound = true, value = "classpath:application-cert.properties")
public class CertificadoMasivoController {

	@Value("${ws.ruta_local}")
	private String RUTA_LOCAL;
	@Value("${ws.temp_zip}")
	private String TEMP_ZIP;
	@Value("${ws.clave}")
	private String clave;
	@Autowired
	CertificadoRep bd_certificado;
	@Autowired
	ProfesorRep bd_profesor;
	@Autowired
	CursoRep bd_curso;
	@Autowired
	Logging log;
	@Autowired
	Url_wsRep urls;

	private int[] extraccionPorURL(String URL_RSM, JSONObject json) throws Exception {
		HttpClient client = HttpClients.createDefault();
		HttpPost post = new HttpPost(URL_RSM);
		// System.out.println(json.toString());

		List<NameValuePair> params = new ArrayList<NameValuePair>(2);
		params.add(new BasicNameValuePair("json", json.toString()));
		params.add(new BasicNameValuePair("clave", clave));
		post.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
		// esperar respuesta
		HttpResponse response = client.execute(post);

		// BufferedReader rd = new BufferedReader(new
		// InputStreamReader(response.getEntity().getContent(), "ISO_8859_1"));
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		String jsonText = "";
		String linea = null;
		while ((linea = rd.readLine()) != null) {
			// System.out.println(linea);
			jsonText += linea;
		}
		JSONObject json_r = null;
		try {
			json_r = new JSONObject(jsonText);
		} catch (Exception e) {
			System.out.println(jsonText);
			return new int[] { 0, 0 };
		}
		// JSONObject json_r = new JSONObject(jsonText);
		String msg = (String) json_r.get("mensaje");
		System.out.println(msg);
		// msg = new
		// String(java.util.Base64.getDecoder().decode(msg),Charset.forName("UTF-8"));
		if (!msg.equals("NULL")) {
			/*
			 * System.out.println("No hay certificados nuevos!");
			 * log.setTrace(LogTypes.EXTRACCION_CONSTANCIAS_NUEVAS,
			 * "0 constancias nuevas extraídas de " + nuevas + " solicitadas");
			 * log.setTrace(LogTypes.EXTRACCION_CONSTANCIAS_ACTUALIZACION,
			 * "0 constancias actualizadas de " + actual + " solicitadas");
			 */
			return new int[] { 0, 0 };
		}

		String mns = (String) json_r.get("zip");
		// System.out.println(mns);

		byte[] bytearray = java.util.Base64.getDecoder().decode(mns);
		String path = RUTA_LOCAL + "certificados.zip";
		File tempdir = new File(RUTA_LOCAL);
		if (!tempdir.exists()) {
			tempdir.mkdirs();
		}
		File out = new File(path);
		try (FileOutputStream os = new FileOutputStream(out)) {
			os.write(bytearray);
			System.out.println("Archivo escrito!");
		} catch (Exception e) {
			System.out.println(e);
		}
		byte[] buffer = new byte[1024];
		File tmp = new File(TEMP_ZIP);
		if (!tmp.exists()) {
			tmp.mkdirs();
		}
		try {
			ZipFile zpf = zpf = new ZipFile(out, Charset.forName("Cp437"));

			Enumeration e = zpf.entries();
			ZipEntry ze;
			// System.out.println("PASE");
			while (e.hasMoreElements()) {
				ze = (ZipEntry) e.nextElement();
				String fileName = ze.getName();
				File newFile = new File(TEMP_ZIP + fileName);
				new File(newFile.getParent()).mkdirs();

				BufferedInputStream bis = new BufferedInputStream(zpf.getInputStream(ze));
				FileOutputStream fos = new FileOutputStream(newFile);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				int len;
				while ((len = bis.read(buffer, 0, 1024)) != -1) {
					fos.write(buffer, 0, len);
					// System.out.println("Escribiendo");
				}
				bos.flush();
				bos.close();
				bis.close();
			}
		} catch (java.util.zip.ZipException zx) {
			System.out.println("Zip file is empty!");
			return new int[] { 0, 0 };
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return new int[] { 0, 0 };
		}
		int nuevas2 = 0;
		int actual2 = 0;
		// comienza a mover los pdfs a la ruta elegida
		for (File f : tmp.listFiles()) {
			Curso c = bd_curso.findByID(Integer.parseInt(f.getName()));
			// Curso c = bd_curso.findByNombre(f.getName());
			JSONObject ar = new JSONObject(json_r.get(f.getName()).toString());
			for (File f2 : f.listFiles()) {
				Profesor p = bd_profesor.findByCorreo(f2.getName());
				for (File f3 : f2.listFiles()) {
					String pt = RUTA_LOCAL + c.getNombre() + "/" + c.getNombre() + "_" + p.getPk_id_profesor() + ".pdf"; // +
																															// f3.getName();
					FileInputStream fs = new FileInputStream(f3);
					File aux = new File(pt);
					new File(aux.getParent()).mkdirs();
					if (aux.exists()) {
						aux.delete();
					}
					try (FileOutputStream os = new FileOutputStream(aux)) {
						os.write(fs.readAllBytes());
					} catch (Exception e) {
						System.out.println(e);
					}
					SeguridadPDF spdf = new SeguridadPDF();
					String nombrec = p.getNombre() + " " + p.getApellido_paterno() + " " + p.getApellido_materno();
					spdf.cifraPdf(pt, nombrec, c.getNombre());
					f3.delete();// elimina archivo
					Certificado cert = bd_certificado.findByRuta(pt);
					if (cert == null) {
						System.out.println("insertando nuevo certificado!");
						cert = new Certificado();
						nuevas2++;
					} else {
						actual2++;
					}
					// Certificado cert = new Certificado();
					cert.setRuta(pt);
					long tt = Long.parseLong((String) ar.get(p.getCorreo()));
					cert.setTiempo_creado(tt);
					cert.setFk_id_curso(c);
					cert.setFk_id_profesor(p);
					bd_certificado.save(cert);
				}
				f2.delete();// elimina directorio (usuario)
			}
			f.delete();// elimina directorio padre (curso)
		}
		out.delete();// elimina zip
		/**/
		return new int[] { nuevas2, actual2 };
	}

	/**
	 * Metodo que obtiene certificados masivamente para traer nuevos archivos. (Cada
	 * 2 horas realiza la tarea)
	 * 
	 * @throws Exception
	 */
	@Scheduled(cron = "0 41 19 * * ?")
	public void scheduleTaskWithCronExpression() throws Exception {
		LinkedList<Url_ws> links = new LinkedList<>(urls.findVarios());
		if (links.size() == 0) {
			throw new Exception("No hay urls!");
		}
		JSONObject json = new JSONObject();
		LinkedList<Profesor> profesores = new LinkedList<>(bd_profesor.findAll());
		if (profesores.size() == 0) {
			return;
		}
		int nuevas = 0;
		int actual = 0;
		System.out.println("A punto de buscar profesores.");
		int k = 0;
		for (Profesor p : profesores) {
			LinkedList<Certificado> cert = new LinkedList<>(p.getCertificados());
			if (cert.size() == 0) {
				LinkedList<Inscripcion> ins = new LinkedList<>(p.getInscripciones());
				if (ins.size() == 0) {
					System.out.println("No hay inscripciones asociadas!");
					continue;
				}

				for (Inscripcion i : ins) {
					// int fkc = i.getFk_id_grupo().getFk_id_curso();
					// Curso caux = bd_curso.findByID(fkc);
					Curso caux = i.getFk_id_grupo().getFk_id_curso();
					// if(!caux.getNombre().equals("COSDAC 2018")) {
					// continue;
					// }
					System.out.println("**\n" + p.getCorreo() + "\n" + caux.getNombre() + "\n**");
					json.put("correo" + k, p.getCorreo());
					json.put("curso" + k, caux.getNombre());
					json.put("id_curso" + k, caux.getPk_id_curso());
					json.put("tiempo" + k, 0);
					System.out.println("Se insertaron elementos en el JSON (certificados no presentes)");
					nuevas++;
					k++;
				}
				continue;
			}
			for (Certificado c : cert) {
				System.out.println("**\n" + p.getCorreo() + "\n" + c.getFk_id_curso().getNombre() + "\n**");
				json.put("correo" + k, p.getCorreo());
				json.put("curso" + k, c.getFk_id_curso().getNombre());
				json.put("id_curso" + k, c.getFk_id_curso().getPk_id_curso());
				json.put("tiempo" + k, c.getTiempo_creado());
				System.out.println("Se insertaron elementos en el JSON (certificadospresentes)");
				actual++;
				k++;
			}

		}
		json.put("cuenta", k);
		int nuevas2 = 0;
		int actual2 = 0;
		for (Url_ws u : links) {
			System.out.println("Intentando con " + u.getUrl());
			int[] x = extraccionPorURL(u.getUrl(), json);
			nuevas2 += x[0];
			actual2 += x[1];
		}
		// if (nuevas != 0) {
		log.setTrace(LogTypes.EXTRACCION_CONSTANCIAS_NUEVAS,
				nuevas2 + " constancias nuevas extraídas de " + nuevas + "solicitadas");
		// }
		// if (actual != 0) {
		log.setTrace(LogTypes.EXTRACCION_CONSTANCIAS_ACTUALIZACION,
				actual2 + "constancias actualizadas de " + actual + "solicitadas");
		// }
		// tarea completada*/
	}
}