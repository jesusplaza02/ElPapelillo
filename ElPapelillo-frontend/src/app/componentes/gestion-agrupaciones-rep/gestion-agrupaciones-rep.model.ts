export interface Agrupacion {
  idAgrupacion: number;
  nombre: string;
  anio: number;
  estadoInscripcion: string;
  categoria: string;
  tipo: string;
  // Usamos any para simplificar el acceso a las propiedades internas en el HTML
  concurso: any; 
  fianza: any;

  agrupacionCanto?: any;
  agrupacionDioses?: any;
}