// Primero definimos cómo es la Agrupación
export interface Agrupacion {
  idAgrupacion?: number;
  nombre: string;
  categoria: string;
  tipo?: string;
  nombreUltimaParticipacion?: string;
  representante?: {          // ¡AÑADIDO!
    idUsuario: number;
    nombre?: string;
  };
  agrupacionCanto?: any;
  agrupacionDrag?: any;
  agrupacionDioses?: any;
}

// Ahora definimos la Inscripción, que es lo que nos devuelve el Backend
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
  agrupacion: Agrupacion; // La agrupación va por dentro (y el repre dentro de ella)
  fianza?: {
    pagada: boolean;
  };
}