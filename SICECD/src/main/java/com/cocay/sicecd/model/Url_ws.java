package com.cocay.sicecd.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Url_ws")
public class Url_ws {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pk_id_url_ws")
	private int pk_id_url_ws;
	@Column(name = "url")
	private String url;
	@Column(name = "varios")
	private boolean varios;
	@Column(name = "activa")
	private boolean activa;
	public int getPk_id_url_ws() {
		return pk_id_url_ws;
	}
	public void setPk_id_url_ws(int pk_id_url_ws) {
		this.pk_id_url_ws = pk_id_url_ws;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public boolean isVarios() {
		return varios;
	}
	public void setVarios(boolean varios) {
		this.varios = varios;
	}
	public boolean isActiva() {
		return activa;
	}
	public void setActiva(boolean activa) {
		this.activa = activa;
	}
	
	

}
