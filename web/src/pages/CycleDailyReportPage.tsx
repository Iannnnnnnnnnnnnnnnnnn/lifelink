import {
  CalendarOutlined,
  FileTextOutlined,
  HeartOutlined,
  ReloadOutlined,
  WarningOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Col,
  DatePicker,
  Empty,
  Grid,
  Row,
  Skeleton,
  Space,
  Tag,
  Typography,
  message,
} from 'antd';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import {
  CycleCareAccess,
  CycleDailyAdviceReport,
  getCycleCareAccess,
  getCycleDailyReport,
  getCycleDailyReports,
  getLatestCycleDailyReport,
  getLatestPartnerCycleDailyReport,
  regenerateCycleDailyReport,
} from '../api/cycleCare';
import { getRelationships, type RelationshipSummary } from '../api/relationship';
import { formatDate } from '../utils/date';

export function CycleDailyReportPage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  const [messageApi, contextHolder] = message.useMessage();
  const [access, setAccess] = useState<CycleCareAccess | null>(null);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [selectedSpaceId, setSelectedSpaceId] = useState<number | undefined>();
  const [selectedDate, setSelectedDate] = useState<Dayjs>(dayjs().subtract(1, 'day'));
  const [report, setReport] = useState<CycleDailyAdviceReport | null>(null);
  const [history, setHistory] = useState<CycleDailyAdviceReport[]>([]);
  const [partnerReport, setPartnerReport] = useState<CycleDailyAdviceReport | null>(null);
  const [loading, setLoading] = useState(false);
  const [regenerating, setRegenerating] = useState(false);

  const loverRelationships = useMemo(() => {
    const allowedIds = access?.loverSpaceIds || [];
    return relationships.filter((item) => allowedIds.includes(item.id));
  }, [access?.loverSpaceIds, relationships]);

  const selectedSpaceName = relationships.find((item) => item.id === selectedSpaceId)?.name;

  const riskColor = (risk?: string) => {
    if (risk === 'HIGH') return 'red';
    if (risk === 'MEDIUM') return 'orange';
    if (risk === 'LOW') return 'blue';
    return 'green';
  };

  const loadPartnerReport = async (spaceId?: number) => {
    if (!spaceId) {
      setPartnerReport(null);
      return;
    }
    try {
      const response = await getLatestPartnerCycleDailyReport(spaceId);
      setPartnerReport(response.data.data);
    } catch (error) {
      setPartnerReport(null);
    }
  };

  const loadReports = async () => {
    const [latestResponse, historyResponse] = await Promise.all([
      getLatestCycleDailyReport(),
      getCycleDailyReports({
        startDate: dayjs().subtract(30, 'day').format('YYYY-MM-DD'),
        endDate: dayjs().format('YYYY-MM-DD'),
      }),
    ]);
    const latest = latestResponse.data.data;
    setHistory(historyResponse.data.data);
    setReport(latest);
    if (latest?.reportDate) {
      setSelectedDate(dayjs(latest.reportDate));
    }
  };

  const loadPage = async () => {
    setLoading(true);
    try {
      const [accessResponse, relationshipResponse] = await Promise.all([getCycleCareAccess(), getRelationships()]);
      const accessData = accessResponse.data.data;
      const relationshipData = relationshipResponse.data.data;
      setAccess(accessData);
      setRelationships(relationshipData);
      if (!accessData.enabled) {
        return;
      }
      const defaultSpaceId = accessData.loverSpaceIds[0];
      setSelectedSpaceId(defaultSpaceId);
      await Promise.all([loadReports(), loadPartnerReport(defaultSpaceId)]);
    } catch (error) {
      messageApi.error(t('cycle.dailyReport.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleDateChange = async (value: Dayjs | null) => {
    const nextDate = value || dayjs().subtract(1, 'day');
    setSelectedDate(nextDate);
    try {
      const response = await getCycleDailyReport(nextDate.format('YYYY-MM-DD'));
      setReport(response.data.data);
    } catch (error) {
      messageApi.error(t('cycle.dailyReport.loadFailed'));
    }
  };

  const handleRegenerateYesterday = async () => {
    const dateText = dayjs().subtract(1, 'day').format('YYYY-MM-DD');
    setRegenerating(true);
    try {
      const response = await regenerateCycleDailyReport(dateText);
      setReport(response.data.data);
      setSelectedDate(dayjs(dateText));
      messageApi.success(t('cycle.dailyReport.regenerated'));
      await Promise.all([loadReports(), loadPartnerReport(selectedSpaceId)]);
    } catch (error) {
      messageApi.error(t('cycle.dailyReport.regenerateFailed'));
    } finally {
      setRegenerating(false);
    }
  };

  useEffect(() => {
    loadPage();
  }, []);

  const adviceItems = [
    ['clothingAdvice', report?.clothingAdvice],
    ['foodAdvice', report?.foodAdvice],
    ['restAdvice', report?.restAdvice],
    ['moodAdvice', report?.moodAdvice],
  ];

  if (loading && !access) {
    return (
      <div className="page-wide cycle-page">
        {contextHolder}
        <Skeleton active paragraph={{ rows: 12 }} />
      </div>
    );
  }

  if (access && !access.enabled) {
    return (
      <div className="page-wide cycle-page">
        {contextHolder}
        <Card>
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description={access.reason === 'NO_LOVER_SPACE' ? t('cycle.accessDenied') : access.reason || t('cycle.accessDenied')}
          >
            <Button type="primary" onClick={() => navigate('/relationships/create')}>
              {t('cycle.createCoupleSpace')}
            </Button>
          </Empty>
        </Card>
      </div>
    );
  }

  return (
    <Space direction="vertical" size={16} className="page-wide cycle-page cycle-daily-page">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('cycle.dailyReport.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('cycle.dailyReport.subtitle')}</Typography.Text>
        </div>
        <Space wrap className={isMobile ? 'cycle-daily-actions-mobile' : undefined}>
          <DatePicker value={selectedDate} onChange={handleDateChange} className="cycle-daily-date-picker" />
          <Button icon={<ReloadOutlined />} onClick={loadPage} loading={loading}>
            {t('common.refresh')}
          </Button>
          <Button type="primary" icon={<FileTextOutlined />} loading={regenerating} onClick={handleRegenerateYesterday}>
            {t('cycle.dailyReport.regenerateYesterday')}
          </Button>
        </Space>
      </div>

      <Alert showIcon type="info" message={report?.disclaimer || t('cycle.dailyReport.disclaimer')} />

      {report ? (
        <>
          <Row gutter={[16, 16]}>
            <Col xs={24} md={8}>
              <Card className="cycle-summary-card">
                <Space direction="vertical" size={8}>
                  <span className="cycle-card-icon"><CalendarOutlined /></span>
                  <Typography.Text type="secondary">{t('cycle.dailyReport.reportDate')}</Typography.Text>
                  <Typography.Title level={3}>
                    {formatDate(report.reportDate, t, i18n.resolvedLanguage)}
                  </Typography.Title>
                  <Typography.Text>{selectedSpaceName || t('cycle.selectLoverSpace')}</Typography.Text>
                </Space>
              </Card>
            </Col>
            <Col xs={24} md={8}>
              <Card className="cycle-summary-card">
                <Space direction="vertical" size={8}>
                  <span className="cycle-card-icon cycle-card-icon-blue"><HeartOutlined /></span>
                  <Typography.Text type="secondary">{t('cycle.currentPhase')}</Typography.Text>
                  <Typography.Title level={3}>{report.phaseLabel || t('cycle.unknownPhase')}</Typography.Title>
                  <Tag color={report.predictedPhase ? 'purple' : 'cyan'}>
                    {report.predictedPhase ? t('cycle.dailyReport.predictedPhase') : t('cycle.dailyReport.recordedPhase')}
                  </Tag>
                </Space>
              </Card>
            </Col>
            <Col xs={24} md={8}>
              <Card className="cycle-summary-card">
                <Space direction="vertical" size={8}>
                  <span className="cycle-card-icon cycle-card-icon-orange"><WarningOutlined /></span>
                  <Typography.Text type="secondary">{t('cycle.dailyReport.riskLevel')}</Typography.Text>
                  <Typography.Title level={3}>
                    <Tag color={riskColor(report.riskLevel)}>{t(`cycle.riskLevels.${report.riskLevel}`)}</Tag>
                  </Typography.Title>
                  <Typography.Text>{report.aiGenerated ? t('cycle.dailyReport.aiEnhanced') : t('cycle.dailyReport.ruleBased')}</Typography.Text>
                </Space>
              </Card>
            </Col>
          </Row>

          <Card title={t('cycle.dailyReport.yesterdaySummary')}>
            <Typography.Paragraph className="cycle-daily-summary">{report.summary}</Typography.Paragraph>
            <Row gutter={[12, 12]}>
              {[
                ['bodyStatusSummary', report.bodyStatusSummary],
                ['flowSummary', report.flowSummary],
                ['painSummary', report.painSummary],
                ['moodSummary', report.moodSummary],
                ['symptomSummary', report.symptomSummary],
              ].map(([key, value]) => (
                <Col xs={24} md={12} lg={8} key={key}>
                  <div className="cycle-advice-item cycle-daily-info-item">
                    <Typography.Text strong>{t(`cycle.dailyReport.${key}`)}</Typography.Text>
                    <Typography.Paragraph>{value || '-'}</Typography.Paragraph>
                  </div>
                </Col>
              ))}
            </Row>
          </Card>

          <Card title={t('cycle.dailyReport.todayAdvice')}>
            <Row gutter={[12, 12]}>
              {adviceItems.map(([key, value]) => (
                <Col xs={24} md={12} key={key}>
                  <div className="cycle-advice-item">
                    <Typography.Text strong>{t(`cycle.${key}`)}</Typography.Text>
                    <Typography.Paragraph>{value || '-'}</Typography.Paragraph>
                  </div>
                </Col>
              ))}
            </Row>
          </Card>

          <Row gutter={[16, 16]}>
            <Col xs={24} lg={12}>
              <Card title={t('cycle.dailyReport.warningSummary')}>
                {report.warningTypes?.length ? (
                  <Space direction="vertical" size={8}>
                    <Space wrap>
                      {report.warningTypes.map((item) => <Tag color="orange" key={item}>{item}</Tag>)}
                    </Space>
                    <Typography.Paragraph>{report.warningSummary}</Typography.Paragraph>
                  </Space>
                ) : (
                  <Typography.Paragraph>{report.warningSummary || t('cycle.noWarnings')}</Typography.Paragraph>
                )}
              </Card>
            </Col>
            <Col xs={24} lg={12}>
              <Card title={t('cycle.partnerAdvice')}>
                <Typography.Paragraph>{report.partnerAdvice || report.partnerVisibleSummary || '-'}</Typography.Paragraph>
                {partnerReport && (
                  <Alert
                    type="success"
                    showIcon
                    message={t('cycle.dailyReport.partnerView')}
                    description={partnerReport.partnerVisibleSummary || partnerReport.summary}
                  />
                )}
              </Card>
            </Col>
          </Row>
        </>
      ) : (
        <Card>
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('cycle.dailyReport.noReport')}>
            <Button type="primary" loading={regenerating} onClick={handleRegenerateYesterday}>
              {t('cycle.dailyReport.regenerateYesterday')}
            </Button>
          </Empty>
        </Card>
      )}

      <Card title={t('cycle.dailyReport.history')}>
        {history.length ? (
          <Space wrap>
            {history.map((item) => (
              <Button
                key={item.id}
                type={report?.id === item.id ? 'primary' : 'default'}
                onClick={() => handleDateChange(dayjs(item.reportDate))}
              >
                {formatDate(item.reportDate, t, i18n.resolvedLanguage)}
              </Button>
            ))}
          </Space>
        ) : (
          <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('cycle.dailyReport.noHistory')} />
        )}
      </Card>
    </Space>
  );
}
