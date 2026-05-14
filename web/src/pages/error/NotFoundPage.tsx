import { ErrorState } from '../../components/common/ErrorState';

export function NotFoundPage() {
  return (
    <div className="page-wide">
      <ErrorState type="404" />
    </div>
  );
}
