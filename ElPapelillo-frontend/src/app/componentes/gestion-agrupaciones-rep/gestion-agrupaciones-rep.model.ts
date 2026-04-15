export interface Agrupacion {
  idAgrupacion: number;
  nombre: string;
  anio: number;
  estadoInscripcion: string;
  categoria: string;
  tipo: string;
  nombreUltimaParticipacion?: string; 
  concurso: any; 
  fianza: any;
  // Objetos para que el HTML acceda a modalidad/director/drag
  agrupacionCanto?: any;
  agrupacionDrag?: any;
  agrupacionDioses?: any;
}