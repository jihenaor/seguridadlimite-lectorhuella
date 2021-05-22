package pojo;

public class Huella {
  private Long id;

  private Long idtrabajador;
  
  private String documento;

  private String huella;

  public Huella(Long id, 
          Long idtrabajador,
          String documento) {
    this.id = id;
    this.idtrabajador = idtrabajador;
    this.documento = documento;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getIdtrabajador() {
    return idtrabajador;
  }

  public void setIdtrabajador(Long idtrabajador) {
    this.idtrabajador = idtrabajador;
  }

  public String getHuella() {
    return huella;
  }

  public void setHuella(String huella) {
    this.huella = huella;
  }

  public String getDocumento() {
    return documento;
  }

  public void setDocumento(String documento) {
    this.documento = documento;
  }
}
