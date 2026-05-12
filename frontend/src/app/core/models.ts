export type UserRole = 'DONOR' | 'INSTITUTION' | 'ADMIN';

export interface SessionState {
  username: string;
  role: UserRole;
  actorDocument: string;
  accessToken: string;
  tokenType: 'Bearer';
  expiresAt: number;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthTokenResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: {
    id: number;
    username: string;
    role: UserRole;
    actorDocument: string;
  };
}

export interface OperationalContext {
  donorId: number;
  donorCpf: string;
  institutionId: number;
  actorDocument: string;
  donorLatitude: number;
  donorLongitude: number;
  radiusKm: number;
}

export interface MedicationResponse {
  id: number;
  donorId: number;
  commercialName: string;
  activeIngredient: string;
  concentration: string;
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  quantityAvailable: number;
  medicationTypeCode: number;
  frontPhotoUrl: string;
  blisterPhotoUrl: string;
  statusCode: number;
  status: string;
  createdAt: string;
}

export interface DonationMatchResponse {
  id: number;
  medicationId: number;
  donorId: number;
  institutionId: number;
  radiusKm: number;
  distanceKm: number;
  validationCode: string | null;
  statusCode: number;
  status: string;
  deliveryDeadline: string | null;
}

export interface InstitutionNearbyResponse {
  id: number;
  legalName: string;
  cnpj: string;
  pharmacistName: string;
  distanceKm: number;
}

export interface OcrPreviewResponse {
  lotNumber: string | null;
  expirationDate: string | null;
}

export interface ApiProblem {
  title?: string;
  detail?: string;
  status?: number;
}
