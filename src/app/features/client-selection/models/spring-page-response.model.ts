/**
 * Spring Data Page response structure
 * Matches the response from Spring Boot's Page<T>
 */
export interface SpringPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number | null;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}
