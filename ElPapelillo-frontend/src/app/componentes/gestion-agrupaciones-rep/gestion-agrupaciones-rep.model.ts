// Primero definimos cómo es la Agrupación
export interface Agrupacion {
  idAgrupacion?: number;
  nombre: string;
  categoria: string;
  tipo?: string;
  nombreUltimaParticipacion?: string;
  representante?: {         
    idUsuario: number;
    nombre?: string;
  };
  agrupacionCanto?: any;
  agrupacionDrag?: any;
  agrupacionDioses?: any;
}

export interface Inscripcion {
  idInscripcion: number;
  fechaInscripcion: string;
  estadoInscripcion: string;
  anio?: number;
  concurso: {
    idConcurso: number;
    nombre: string;
    tipoConcurso: string;
  };
  agrupacion: Agrupacion; 
  fianza?: {
    pagada: boolean;
  };
}