

UPDATE classrooms SET homeroom_teacher_id = '44444444-4444-4444-4444-444444444446'
WHERE code = '3A' AND school_id = (SELECT id FROM schools WHERE code = 'SCH-MF03-0001')
  AND homeroom_teacher_id IS NULL;

UPDATE code_sequences SET next_value = GREATEST(next_value, 4) WHERE scope = 'mf09_distribution';
