export interface Concurso {
  idConcurso: number;
  nombre: string;
  fechaInicio: string;
  fechaFin: string;
  fechaInicioInscripcion: string;
  fechaFinInscripcion: string;
  tipoConcurso: string;
  estado: string;
  idOrganizacion: number;
  nombreOrganizacion?: string; // Para mostrar el nombre de la empresa
}