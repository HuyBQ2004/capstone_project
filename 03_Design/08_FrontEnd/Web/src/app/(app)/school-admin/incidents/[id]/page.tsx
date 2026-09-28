import { IncidentDetailView } from "@/components/app/incident-detail-view";

export default async function SchoolAdminIncidentDetail({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <IncidentDetailView incidentId={id} role="school-admin" />;
}
