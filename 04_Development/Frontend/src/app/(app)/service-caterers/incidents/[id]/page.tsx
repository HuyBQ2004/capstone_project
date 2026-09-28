import { IncidentDetailView } from "@/components/app/incident-detail-view";

/**
 * 35_ViewIncidentInformationDetail — MF11 (Service Caterers).
 * Read-only: lô cách ly (BR-149), CAPA phối hợp (BR-152); Caterer không có
 * quyền đóng sự cố (BR-155 — chỉ School Administration).
 */
export default async function CatererIncidentDetail({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <IncidentDetailView incidentId={id} role="service-caterers" />;
}
