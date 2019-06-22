package com.cocay.sicecd.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "Log_sys")
public class Log_sys {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_id_log_sys")
	int pk_id_log_sys;
	@Column(name = "ip")
	String ip;
	@Column(name = "hora")
	Date hora;
	@Column(name = "comentario")
	String comentario;



	@ManyToOne(targetEntity=Usuario_sys.class)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "fk_id_usuario_sys")
	Usuario_sys fk_id_usuario_sys;

	@ManyToOne(targetEntity=Log_evento_sys.class)
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "fk_id_log_evento_sys")
	Log_evento_sys fk_id_log_evento_sys;


	public int getPk_id_log_sys() {
		return pk_id_log_sys;
	}

	public void setPk_id_log_sys(int pk_id_log_sys) {
		this.pk_id_log_sys = pk_id_log_sys;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Date getHora() {
		return hora;
	}

	public void setHora(Date hora) {
		this.hora = hora;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Usuario_sys getFk_id_usuario_sys() {
		return fk_id_usuario_sys;
	}

	public void setFk_id_usuario_sys(Usuario_sys fk_id_usuario_sys) {
		this.fk_id_usuario_sys = fk_id_usuario_sys;
	}

	public Log_evento_sys getFk_id_log_evento_sys() {
		return fk_id_log_evento_sys;
	}

	public void setFk_id_log_evento_sys(Log_evento_sys fk_id_log_evento_sys) {
		this.fk_id_log_evento_sys = fk_id_log_evento_sys;
	}
}
