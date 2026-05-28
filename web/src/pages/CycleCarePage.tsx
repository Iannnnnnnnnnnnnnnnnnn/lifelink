import {
  AlertOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  DeleteOutlined,
  EditOutlined,
  FileTextOutlined,
  HeartOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import {
  Alert,
  Button,
  Card,
  Col,
  DatePicker,
  Empty,
  Form,
  Grid,
  Input,
  InputNumber,
  Modal,
  Popconfirm,
  Row,
  Select,
  Skeleton,
  Slider,
  Space,
  Switch,
  Table,
  Tabs,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { Dayjs } from 'dayjs';
import dayjs from 'dayjs';
import { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import {
  createCyclePeriodRecord,
  CycleCareAccess,
  CycleBloodColor,
  CycleCareProfile,
  CycleDailyLog,
  CycleFlowLevel,
  CyclePeriodRecord,
  CycleShareLevel,
  CycleToday,
  deleteCyclePeriodRecord,
  dismissCycleWarning,
  getCycleCareAccess,
  getCycleCareProfile,
  getCycleDailyLog,
  getCyclePeriodRecords,
  getCycleToday,
  updateCyclePeriodRecord,
  upsertCycleCareProfile,
  upsertCycleDailyLog,
  parseCycleLog,
} from '../api/cycleCare';
import { getRelationships, type RelationshipSummary } from '../api/relationship';
import { formatDate } from '../utils/date';

type ProfileFormValues = {
  defaultLoverSpaceId?: number;
  cycleLength?: number;
  periodLength?: number;
  lastPeriodStartDate?: Dayjs;
  reminderEnabled?: boolean;
  dailyAdviceEnabled?: boolean;
  shareLevel?: CycleShareLevel;
  privacyNoteVisibleToPartner?: boolean;
};

type DailyLogFormValues = {
  logDate: Dayjs;
  flowLevel: CycleFlowLevel;
  bloodColor?: CycleBloodColor;
  painLevel?: number;
  mood?: string;
  symptoms?: string[];
  temperatureFeeling?: string;
  appetite?: string;
  sleepHours?: number;
  waterCups?: number;
  exerciseMinutes?: number;
  foodTags?: string[];
  medicationNote?: string;
  dischargeNote?: string;
  temperature?: number;
  weight?: number;
  note?: string;
};

type PeriodRecordFormValues = {
  startDate: Dayjs;
  endDate?: Dayjs;
  flowSummary?: string;
  painSummary?: string;
  colorSummary?: string;
  note?: string;
};

const flowLevels: CycleFlowLevel[] = ['NONE', 'LIGHT', 'MEDIUM', 'HEAVY', 'VERY_HEAVY'];
const bloodColors = ['BRIGHT_RED', 'DARK_RED', 'BROWN', 'PINK', 'OTHER'];
const shareLevels: CycleShareLevel[] = ['PRIVATE', 'SUMMARY', 'CALENDAR_ONLY', 'FULL'];
const moodOptions = ['CALM', 'HAPPY', 'TIRED', 'ANXIOUS', 'IRRITABLE', 'SAD', 'STRESSED'];
const symptomOptions = ['cramps', 'headache', 'backache', 'tired', 'bloating', 'fever', 'sick'];
const foodOptions = ['light', 'spicy', 'cold', 'sweet', 'protein', 'warm'];

export function CycleCarePage() {
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const screens = Grid.useBreakpoint();
  const isMobile = !screens.md;
  const [messageApi, contextHolder] = message.useMessage();
  const [profileForm] = Form.useForm<ProfileFormValues>();
  const [dailyForm] = Form.useForm<DailyLogFormValues>();
  const [recordForm] = Form.useForm<PeriodRecordFormValues>();
  const [access, setAccess] = useState<CycleCareAccess | null>(null);
  const [relationships, setRelationships] = useState<RelationshipSummary[]>([]);
  const [profile, setProfile] = useState<CycleCareProfile | null>(null);
  const [today, setToday] = useState<CycleToday | null>(null);
  const [dailyLog, setDailyLog] = useState<CycleDailyLog | null>(null);
  const [records, setRecords] = useState<CyclePeriodRecord[]>([]);
  const [selectedLoverSpaceId, setSelectedLoverSpaceId] = useState<number | undefined>();
  const [dailyDate, setDailyDate] = useState<Dayjs>(dayjs());
  const [loading, setLoading] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingDailyLog, setSavingDailyLog] = useState(false);
  const [parsingLog, setParsingLog] = useState(false);
  const [naturalLogText, setNaturalLogText] = useState('');
  const [savingRecord, setSavingRecord] = useState(false);
  const [recordModalOpen, setRecordModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<CyclePeriodRecord | null>(null);

  const loverRelationships = useMemo(() => {
    const allowedIds = access?.loverSpaceIds || [];
    return relationships.filter((item) => allowedIds.includes(item.id));
  }, [access?.loverSpaceIds, relationships]);

  const relationshipOptions = loverRelationships.map((item) => ({ value: item.id, label: item.name }));

  const getRelationshipName = (id?: number) => {
    if (!id) return '-';
    return relationships.find((item) => item.id === id)?.name || String(id);
  };

  const fillDailyForm = (log: CycleDailyLog | null, date: Dayjs) => {
    dailyForm.setFieldsValue({
      logDate: date,
      flowLevel: log?.flowLevel || 'NONE',
      bloodColor: log?.bloodColor,
      painLevel: log?.painLevel ?? 0,
      mood: log?.mood,
      symptoms: log?.symptoms || [],
      temperatureFeeling: log?.temperatureFeeling,
      appetite: log?.appetite,
      sleepHours: log?.sleepHours,
      waterCups: log?.waterCups,
      exerciseMinutes: log?.exerciseMinutes,
      foodTags: log?.foodTags || [],
      medicationNote: log?.medicationNote,
      dischargeNote: log?.dischargeNote,
      temperature: log?.temperature,
      weight: log?.weight,
      note: log?.note,
    });
  };

  const loadCycleData = async (loverSpaceId?: number, date = dailyDate) => {
    if (!loverSpaceId) return;
    const dateText = date.format('YYYY-MM-DD');
    const [todayResponse, recordsResponse, dailyLogResponse] = await Promise.all([
      getCycleToday(loverSpaceId),
      getCyclePeriodRecords({ loverSpaceId, page: 1, size: 50 }),
      getCycleDailyLog(dateText),
    ]);
    setToday(todayResponse.data.data);
    setRecords(recordsResponse.data.data);
    setDailyLog(dailyLogResponse.data.data);
    fillDailyForm(dailyLogResponse.data.data, date);
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
      const profileResponse = await getCycleCareProfile();
      const profileData = profileResponse.data.data;
      const loverSpaceId = profileData.defaultLoverSpaceId || accessData.loverSpaceIds[0];
      setProfile(profileData);
      setSelectedLoverSpaceId(loverSpaceId);
      profileForm.setFieldsValue({
        defaultLoverSpaceId: loverSpaceId,
        cycleLength: profileData.cycleLength,
        periodLength: profileData.periodLength,
        lastPeriodStartDate: profileData.lastPeriodStartDate ? dayjs(profileData.lastPeriodStartDate) : undefined,
        reminderEnabled: profileData.reminderEnabled,
        dailyAdviceEnabled: profileData.dailyAdviceEnabled,
        shareLevel: profileData.shareLevel,
        privacyNoteVisibleToPartner: profileData.privacyNoteVisibleToPartner,
      });
      await loadCycleData(loverSpaceId);
    } catch (error) {
      messageApi.error(t('cycle.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleLoverSpaceChange = async (loverSpaceId: number) => {
    setSelectedLoverSpaceId(loverSpaceId);
    profileForm.setFieldValue('defaultLoverSpaceId', loverSpaceId);
    setLoading(true);
    try {
      await loadCycleData(loverSpaceId);
    } catch (error) {
      messageApi.error(t('cycle.loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleSaveProfile = async () => {
    const values = await profileForm.validateFields();
    setSavingProfile(true);
    try {
      const response = await upsertCycleCareProfile({
        defaultLoverSpaceId: values.defaultLoverSpaceId,
        cycleLength: values.cycleLength,
        periodLength: values.periodLength,
        lastPeriodStartDate: values.lastPeriodStartDate?.format('YYYY-MM-DD'),
        reminderEnabled: values.reminderEnabled,
        dailyAdviceEnabled: values.dailyAdviceEnabled,
        shareLevel: values.shareLevel,
        timezone: 'Asia/Shanghai',
        privacyNoteVisibleToPartner: values.privacyNoteVisibleToPartner,
      });
      const nextProfile = response.data.data;
      setProfile(nextProfile);
      setSelectedLoverSpaceId(nextProfile.defaultLoverSpaceId);
      messageApi.success(t('cycle.profileSaved'));
      await loadCycleData(nextProfile.defaultLoverSpaceId);
    } catch (error) {
      messageApi.error(t('cycle.saveFailed'));
    } finally {
      setSavingProfile(false);
    }
  };

  const handleDailyDateChange = async (value: Dayjs | null) => {
    const nextDate = value || dayjs();
    setDailyDate(nextDate);
    try {
      const response = await getCycleDailyLog(nextDate.format('YYYY-MM-DD'));
      setDailyLog(response.data.data);
      fillDailyForm(response.data.data, nextDate);
    } catch (error) {
      messageApi.error(t('cycle.loadFailed'));
    }
  };

  const handleSaveDailyLog = async () => {
    if (!selectedLoverSpaceId) return;
    const values = await dailyForm.validateFields();
    const dateText = values.logDate.format('YYYY-MM-DD');
    setSavingDailyLog(true);
    try {
      const response = await upsertCycleDailyLog(dateText, {
        loverSpaceId: selectedLoverSpaceId,
        flowLevel: values.flowLevel,
        bloodColor: values.bloodColor,
        painLevel: values.painLevel,
        mood: values.mood,
        symptoms: values.symptoms,
        temperatureFeeling: values.temperatureFeeling,
        appetite: values.appetite,
        sleepHours: values.sleepHours,
        waterCups: values.waterCups,
        exerciseMinutes: values.exerciseMinutes,
        foodTags: values.foodTags,
        medicationNote: values.medicationNote,
        dischargeNote: values.dischargeNote,
        temperature: values.temperature,
        weight: values.weight,
        note: values.note,
      });
      setDailyLog(response.data.data);
      messageApi.success(t('cycle.dailyLogSaved'));
      await loadCycleData(selectedLoverSpaceId, values.logDate);
    } catch (error) {
      messageApi.error(t('cycle.saveFailed'));
    } finally {
      setSavingDailyLog(false);
    }
  };

  const handleParseNaturalLog = async () => {
    const text = naturalLogText.trim();
    if (!text) return;
    setParsingLog(true);
    try {
      const response = await parseCycleLog(text);
      const parsed = response.data.data;
      dailyForm.setFieldsValue({
        flowLevel: parsed.flowLevel || dailyForm.getFieldValue('flowLevel') || 'NONE',
        bloodColor: parsed.bloodColor,
        painLevel: parsed.painLevel ?? dailyForm.getFieldValue('painLevel'),
        mood: parsed.mood,
        sleepHours: parsed.sleepHours,
        waterCups: parsed.waterCups,
        exerciseMinutes: parsed.exerciseMinutes,
        symptoms: parsed.symptoms || [],
        foodTags: parsed.foodTags || [],
        note: parsed.note || text,
      });
      messageApi.success(t('cycle.parseApplied'));
    } catch (error) {
      messageApi.error(t('cycle.parseFailed'));
    } finally {
      setParsingLog(false);
    }
  };

  const openCreateRecord = () => {
    setEditingRecord(null);
    recordForm.resetFields();
    recordForm.setFieldsValue({ startDate: dayjs(), endDate: undefined, note: undefined });
    setRecordModalOpen(true);
  };

  const openEditRecord = (record: CyclePeriodRecord) => {
    setEditingRecord(record);
    recordForm.setFieldsValue({
      startDate: dayjs(record.startDate),
      endDate: record.endDate ? dayjs(record.endDate) : undefined,
      note: record.note,
      flowSummary: record.flowSummary,
      painSummary: record.painSummary,
      colorSummary: record.colorSummary,
    });
    setRecordModalOpen(true);
  };

  const handleSaveRecord = async () => {
    if (!selectedLoverSpaceId) return;
    const values = await recordForm.validateFields();
    setSavingRecord(true);
    try {
      const payload = {
        loverSpaceId: selectedLoverSpaceId,
        startDate: values.startDate.format('YYYY-MM-DD'),
        endDate: values.endDate?.format('YYYY-MM-DD'),
        flowSummary: values.flowSummary,
        painSummary: values.painSummary,
        colorSummary: values.colorSummary,
        note: values.note,
      };
      if (editingRecord) {
        await updateCyclePeriodRecord(editingRecord.id, payload);
        messageApi.success(t('cycle.recordUpdated'));
      } else {
        await createCyclePeriodRecord(payload);
        messageApi.success(t('cycle.recordCreated'));
      }
      setRecordModalOpen(false);
      await loadCycleData(selectedLoverSpaceId);
    } catch (error) {
      messageApi.error(t('cycle.saveFailed'));
    } finally {
      setSavingRecord(false);
    }
  };

  const handleDeleteRecord = async (record: CyclePeriodRecord) => {
    if (!selectedLoverSpaceId) return;
    try {
      await deleteCyclePeriodRecord(record.id);
      messageApi.success(t('cycle.recordDeleted'));
      await loadCycleData(selectedLoverSpaceId);
    } catch (error) {
      messageApi.error(t('cycle.deleteFailed'));
    }
  };

  const handleDismissWarning = async (warningId: number) => {
    if (!selectedLoverSpaceId) return;
    try {
      await dismissCycleWarning(warningId);
      messageApi.success(t('cycle.warningDismissed'));
      await loadCycleData(selectedLoverSpaceId);
    } catch (error) {
      messageApi.error(t('cycle.saveFailed'));
    }
  };

  useEffect(() => {
    loadPage();
  }, []);

  const recordColumns: ColumnsType<CyclePeriodRecord> = [
    {
      title: t('cycle.startDate'),
      dataIndex: 'startDate',
      render: (value: string) => formatDate(value, t, i18n.resolvedLanguage),
    },
    {
      title: t('cycle.endDate'),
      dataIndex: 'endDate',
      render: (value?: string) => (value ? formatDate(value, t, i18n.resolvedLanguage) : '-'),
    },
    {
      title: t('cycle.duration'),
      render: (_, record) => {
        const end = record.endDate ? dayjs(record.endDate) : dayjs(record.startDate);
        return t('cycle.daysCount', { count: end.diff(dayjs(record.startDate), 'day') + 1 });
      },
    },
    {
      title: t('cycle.relationship'),
      dataIndex: 'loverSpaceId',
      render: (value?: number) => getRelationshipName(value),
    },
    {
      title: t('cycle.note'),
      dataIndex: 'note',
      ellipsis: true,
      render: (value?: string) => value || '-',
    },
    {
      title: t('common.edit'),
      key: 'actions',
      width: 150,
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEditRecord(record)} />
          <Popconfirm title={t('cycle.deleteRecordConfirm')} onConfirm={() => handleDeleteRecord(record)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const warningColor = (severity?: string) => {
    if (severity === 'HIGH') return 'red';
    if (severity === 'MEDIUM') return 'orange';
    return 'blue';
  };

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
    <Space direction="vertical" size={16} className="page-wide cycle-page">
      {contextHolder}
      <div className="page-heading">
        <div>
          <Typography.Title level={2}>{t('cycle.title')}</Typography.Title>
          <Typography.Text type="secondary">{t('cycle.subtitle')}</Typography.Text>
        </div>
        <Space wrap>
          <Select
            className="cycle-space-select"
            value={selectedLoverSpaceId}
            options={relationshipOptions}
            placeholder={t('cycle.selectLoverSpace')}
            onChange={handleLoverSpaceChange}
          />
          <Button icon={<ReloadOutlined />} onClick={loadPage} loading={loading}>
            {t('common.refresh')}
          </Button>
          <Button icon={<FileTextOutlined />} onClick={() => navigate('/cycle-care/daily')}>
            {t('cycle.dailyReport.title')}
          </Button>
        </Space>
      </div>

      {today?.disclaimer && <Alert showIcon type="info" message={today.disclaimer} />}

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={8}>
          <Card className="cycle-summary-card">
            <Space direction="vertical" size={8}>
              <span className="cycle-card-icon"><HeartOutlined /></span>
              <Typography.Text type="secondary">{t('cycle.currentPhase')}</Typography.Text>
              <Typography.Title level={3}>{today?.phaseLabel || t('cycle.unknownPhase')}</Typography.Title>
              <Typography.Text>{today?.title || t('cycle.completeProfileHint')}</Typography.Text>
              {today?.predictedNextStartDate && (
                <Tag color="magenta">
                  {t('cycle.nextPeriod')}: {formatDate(today.predictedNextStartDate, t, i18n.resolvedLanguage)}
                </Tag>
              )}
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card className="cycle-summary-card">
            <Space direction="vertical" size={8}>
              <span className="cycle-card-icon cycle-card-icon-blue"><CalendarOutlined /></span>
              <Typography.Text type="secondary">{t('cycle.daysToNextPeriod')}</Typography.Text>
              <Typography.Title level={3}>
                {typeof today?.daysToNextPeriod === 'number' ? t('cycle.daysCount', { count: today.daysToNextPeriod }) : '-'}
              </Typography.Title>
              <Typography.Text>{today?.predicted ? t('cycle.predictedByHistory') : t('cycle.predictedByProfile')}</Typography.Text>
            </Space>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card className="cycle-summary-card">
            <Space direction="vertical" size={8}>
              <span className="cycle-card-icon cycle-card-icon-orange"><AlertOutlined /></span>
              <Typography.Text type="secondary">{t('cycle.activeWarnings')}</Typography.Text>
              <Typography.Title level={3}>{today?.warnings.length || 0}</Typography.Title>
              <Typography.Text>{t('cycle.warningHint')}</Typography.Text>
            </Space>
          </Card>
        </Col>
      </Row>

      <Tabs
        className="content-tabs"
        items={[
          {
            key: 'today',
            label: t('cycle.todayTab'),
            children: (
              <Row gutter={[16, 16]}>
                <Col xs={24} xl={14}>
                  <Card title={t('cycle.careAdvice')}>
                    <Row gutter={[12, 12]}>
                      {[
                        ['clothingAdvice', today?.clothingAdvice],
                        ['foodAdvice', today?.foodAdvice],
                        ['restAdvice', today?.restAdvice],
                        ['moodAdvice', today?.moodAdvice],
                        ['partnerAdvice', today?.partnerAdvice],
                      ].map(([key, value]) => (
                        <Col xs={24} md={12} key={key}>
                          <div className="cycle-advice-item">
                            <Typography.Text strong>{t(`cycle.${key}`)}</Typography.Text>
                            <Typography.Paragraph>{value || '-'}</Typography.Paragraph>
                          </div>
                        </Col>
                      ))}
                    </Row>
                  </Card>
                </Col>
                <Col xs={24} xl={10}>
                  <Card title={t('cycle.warnings')}>
                    {today?.warnings.length ? (
                      <Space direction="vertical" size={10} className="cycle-warning-list">
                        {today.warnings.map((warning) => (
                          <Alert
                            key={warning.id}
                            type={warning.severity === 'HIGH' ? 'error' : warning.severity === 'MEDIUM' ? 'warning' : 'info'}
                            showIcon
                            message={(
                              <Space wrap>
                                <span>{warning.title}</span>
                                <Tag color={warningColor(warning.severity)}>{t(`cycle.severity.${warning.severity}`)}</Tag>
                              </Space>
                            )}
                            description={(
                              <Space direction="vertical" size={8}>
                                <span>{warning.message}</span>
                                <Button size="small" icon={<CheckCircleOutlined />} onClick={() => handleDismissWarning(warning.id)}>
                                  {t('cycle.dismissWarning')}
                                </Button>
                              </Space>
                            )}
                          />
                        ))}
                      </Space>
                    ) : (
                      <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('cycle.noWarnings')} />
                    )}
                  </Card>
                </Col>
              </Row>
            ),
          },
          {
            key: 'daily',
            label: t('cycle.dailyLogTab'),
            children: (
              <Card title={t('cycle.dailyLog')}>
                <Space direction="vertical" size={12} className="form-full-width">
                  <Input.TextArea
                    rows={2}
                    maxLength={1000}
                    showCount
                    value={naturalLogText}
                    onChange={(event) => setNaturalLogText(event.target.value)}
                    placeholder={t('cycle.naturalLogPlaceholder')}
                  />
                  <Button loading={parsingLog} onClick={handleParseNaturalLog}>
                    {t('cycle.parseNaturalLog')}
                  </Button>
                </Space>
                <Form form={dailyForm} layout="vertical" initialValues={{ logDate: dailyDate, flowLevel: 'NONE', painLevel: 0, symptoms: [], foodTags: [] }}>
                  <Row gutter={[16, 0]}>
                    <Col xs={24} md={8}>
                      <Form.Item name="logDate" label={t('cycle.logDate')} rules={[{ required: true, message: t('cycle.logDateRequired') }]}>
                        <DatePicker className="form-full-width" onChange={handleDailyDateChange} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="flowLevel" label={t('cycle.flowLevel')}>
                        <Select options={flowLevels.map((value) => ({ value, label: t(`cycle.flowLevels.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="bloodColor" label={t('cycle.bloodColor')}>
                        <Select allowClear options={bloodColors.map((value) => ({ value, label: t(`cycle.bloodColors.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="painLevel" label={t('cycle.painLevel')}>
                        <Slider min={0} max={10} marks={{ 0: '0', 5: '5', 10: '10' }} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="mood" label={t('cycle.mood')}>
                        <Select allowClear options={moodOptions.map((value) => ({ value, label: t(`cycle.moods.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="temperatureFeeling" label={t('cycle.temperatureFeeling')}>
                        <Select allowClear options={['normal', 'cold', 'hot'].map((value) => ({ value, label: t(`cycle.temperature.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="appetite" label={t('cycle.appetite')}>
                        <Select allowClear options={['normal', 'low', 'high'].map((value) => ({ value, label: t(`cycle.appetiteOptions.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="sleepHours" label={t('cycle.sleepHours')}>
                        <InputNumber className="form-full-width" min={0} max={24} step={0.5} addonAfter={t('cycle.hourUnit')} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="waterCups" label={t('cycle.waterCups')}>
                        <InputNumber className="form-full-width" min={0} max={40} addonAfter={t('cycle.cupUnit')} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="exerciseMinutes" label={t('cycle.exerciseMinutes')}>
                        <InputNumber className="form-full-width" min={0} max={1440} addonAfter={t('cycle.minuteUnit')} />
                      </Form.Item>
                    </Col>
                    <Col xs={24}>
                      <Form.Item name="symptoms" label={t('cycle.symptoms')}>
                        <Select mode="multiple" allowClear options={symptomOptions.map((value) => ({ value, label: t(`cycle.symptomOptions.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24}>
                      <Form.Item name="foodTags" label={t('cycle.foodTags')}>
                        <Select mode="multiple" allowClear options={foodOptions.map((value) => ({ value, label: t(`cycle.foodOptions.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="temperature" label={t('cycle.temperatureValue')}>
                        <InputNumber className="form-full-width" min={30} max={45} step={0.1} addonAfter="℃" />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="weight" label={t('cycle.weight')}>
                        <InputNumber className="form-full-width" min={20} max={300} step={0.1} addonAfter="kg" />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="medicationNote" label={t('cycle.medicationNote')}>
                        <Input maxLength={500} />
                      </Form.Item>
                    </Col>
                    <Col xs={24}>
                      <Form.Item name="dischargeNote" label={t('cycle.dischargeNote')}>
                        <Input maxLength={500} />
                      </Form.Item>
                    </Col>
                    <Col xs={24}>
                      <Form.Item name="note" label={t('cycle.note')}>
                        <Input.TextArea rows={3} maxLength={1000} showCount placeholder={t('cycle.notePlaceholder')} />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Button type="primary" loading={savingDailyLog} onClick={handleSaveDailyLog}>
                    {dailyLog ? t('common.save') : t('cycle.createDailyLog')}
                  </Button>
                </Form>
              </Card>
            ),
          },
          {
            key: 'records',
            label: t('cycle.recordsTab'),
            children: (
              <Card
                title={t('cycle.periodRecords')}
                extra={<Button type="primary" icon={<CalendarOutlined />} onClick={openCreateRecord}>{t('cycle.addPeriodRecord')}</Button>}
              >
                <Table
                  rowKey="id"
                  columns={recordColumns}
                  dataSource={records}
                  pagination={false}
                  loading={loading}
                  scroll={{ x: 860 }}
                  locale={{ emptyText: <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} description={t('cycle.noRecords')} /> }}
                />
              </Card>
            ),
          },
          {
            key: 'profile',
            label: t('cycle.profileTab'),
            children: (
              <Card title={t('cycle.profile')}>
                <Form form={profileForm} layout="vertical">
                  <Row gutter={[16, 0]}>
                    <Col xs={24} md={12}>
                      <Form.Item name="defaultLoverSpaceId" label={t('cycle.defaultLoverSpace')} rules={[{ required: true, message: t('cycle.selectLoverSpace') }]}>
                        <Select options={relationshipOptions} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={6}>
                      <Form.Item name="cycleLength" label={t('cycle.cycleLength')} rules={[{ required: true, message: t('cycle.cycleLengthRequired') }]}>
                        <InputNumber className="form-full-width" min={15} max={60} addonAfter={t('cycle.dayUnit')} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={6}>
                      <Form.Item name="periodLength" label={t('cycle.periodLength')} rules={[{ required: true, message: t('cycle.periodLengthRequired') }]}>
                        <InputNumber className="form-full-width" min={1} max={15} addonAfter={t('cycle.dayUnit')} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="lastPeriodStartDate" label={t('cycle.lastPeriodStartDate')}>
                        <DatePicker className="form-full-width" />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="shareLevel" label={t('cycle.shareLevel')}>
                        <Select options={shareLevels.map((value) => ({ value, label: t(`cycle.shareLevels.${value}`) }))} />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="reminderEnabled" label={t('cycle.reminderEnabled')} valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="dailyAdviceEnabled" label={t('cycle.dailyAdviceEnabled')} valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                    <Col xs={24} md={8}>
                      <Form.Item name="privacyNoteVisibleToPartner" label={t('cycle.privacyNoteVisibleToPartner')} valuePropName="checked">
                        <Switch />
                      </Form.Item>
                    </Col>
                  </Row>
                  <Button type="primary" loading={savingProfile} onClick={handleSaveProfile}>
                    {t('common.save')}
                  </Button>
                </Form>
                {profile && (
                  <Typography.Text type="secondary" className="cycle-profile-meta">
                    {t('cycle.profileUpdatedAt')}: {formatDate(profile.updatedAt, t, i18n.resolvedLanguage)}
                  </Typography.Text>
                )}
              </Card>
            ),
          },
        ]}
      />

      <Modal
        title={editingRecord ? t('cycle.editPeriodRecord') : t('cycle.addPeriodRecord')}
        open={recordModalOpen}
        confirmLoading={savingRecord}
        onOk={handleSaveRecord}
        onCancel={() => setRecordModalOpen(false)}
        okText={t('common.save')}
        cancelText={t('common.cancel')}
        width={isMobile ? '92vw' : 560}
      >
        <Form form={recordForm} layout="vertical">
          <Form.Item name="startDate" label={t('cycle.startDate')} rules={[{ required: true, message: t('cycle.startDateRequired') }]}>
            <DatePicker className="form-full-width" />
          </Form.Item>
          <Form.Item name="endDate" label={t('cycle.endDate')}>
            <DatePicker className="form-full-width" />
          </Form.Item>
          <Form.Item name="flowSummary" label={t('cycle.flowSummary')}>
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item name="painSummary" label={t('cycle.painSummary')}>
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item name="colorSummary" label={t('cycle.colorSummary')}>
            <Input maxLength={100} />
          </Form.Item>
          <Form.Item name="note" label={t('cycle.note')}>
            <Input.TextArea rows={3} maxLength={1000} showCount placeholder={t('cycle.notePlaceholder')} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}
