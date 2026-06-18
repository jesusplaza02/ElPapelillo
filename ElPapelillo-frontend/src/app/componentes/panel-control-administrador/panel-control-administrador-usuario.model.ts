export interface Usuario {
  idUsuario: number;
  DNI: string;
  nombre: string;
  email: string;
  direccion: string;
  fechaRegistro?: string; // El ? significa que es opcional
  rol?: string; // Lo usaremos para diferenciar Admin de Representante
  activo?: number | boolean;
    
}