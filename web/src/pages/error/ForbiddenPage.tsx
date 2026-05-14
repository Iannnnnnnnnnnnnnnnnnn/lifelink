import { ErrorState } from '../../components/common/ErrorState';

export function ForbiddenPage() {
  return (
    <div className="page-wide">
      <ErrorState type="403" />
    </div>
  );
}
