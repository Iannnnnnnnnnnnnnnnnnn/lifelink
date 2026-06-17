import { Navigate, useParams } from 'react-router-dom';

export function RelationshipFinance() {
  const params = useParams();
  const relationshipId = Number(params.relationshipId);

  return <Navigate to={`/finance?scope=space&spaceId=${relationshipId}`} replace />;
}
