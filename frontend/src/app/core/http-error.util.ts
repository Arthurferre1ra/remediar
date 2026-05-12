import { HttpErrorResponse } from '@angular/common/http';
import { ApiProblem } from './models';

export function readHttpError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Nao foi possivel concluir a operacao.';
  }

  if (error.status === 0) {
    return 'Servidor indisponivel ou bloqueado pelo navegador.';
  }

  const problem = error.error as ApiProblem | string | null;
  if (typeof problem === 'string' && problem.trim().length > 0) {
    return problem;
  }

  if (problem && typeof problem === 'object') {
    return problem.detail || problem.title || 'Falha na comunicacao com a API.';
  }

  return error.message || 'Falha na comunicacao com a API.';
}
