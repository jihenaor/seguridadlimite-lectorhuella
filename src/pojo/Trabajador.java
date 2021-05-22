package pojo;

public class Trabajador {
  private Long id;

  private String numerodocumento;
    
  private String nombrecompleto;

  private Long idaprendiz;
    
  private String base64;
    
  private Long idgrupo;
    
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getNumerodocumento() {
    return numerodocumento;
  }

  public void setNumerodocumento(String numerodocumento) {
    this.numerodocumento = numerodocumento;
  }

  public String getNombrecompleto() {
    return nombrecompleto;
  }

  public void setNombrecompleto(String nombrecompleto) {
    this.nombrecompleto = nombrecompleto;
  }

  public Long getIdaprendiz() {
    return idaprendiz;
  }

  public void setIdaprendiz(Long idaprendiz) {
    this.idaprendiz = idaprendiz;
  }

  public String getBase64() {
    return base64;
  }

  public void setBase64(String base64) {
    this.base64 = base64;
  }

  public Long getIdgrupo() {
    return idgrupo;
  }

  public void setIdgrupo(Long idgrupo) {
    this.idgrupo = idgrupo;
  }
}
